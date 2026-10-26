package com.banco.anteojos.backend.useCase.assignments.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banco.anteojos.backend.business.applicants.dto.response.EligibilityResponseDto;
import com.banco.anteojos.backend.business.assignments.FrameReservationServiceHandler;
import com.banco.anteojos.backend.business.assignments.dto.response.FrameReservationResponseDto;
import com.banco.anteojos.backend.business.assignments.entities.FrameReservation;
import com.banco.anteojos.backend.business.assignments.exception.FrameReservationNotAllowedException;
import com.banco.anteojos.backend.business.assignments.exception.FrameReservationNotFoundException;
import com.banco.anteojos.backend.persistence.assignments.FrameReservationPostgresSqlRepository;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class FrameReservationServiceHandlerTest {

	private static final EligibilityResponseDto ELIGIBLE = new EligibilityResponseDto(1L, true, null);

	@Mock
	private FrameReservationPostgresSqlRepository reservationRepository;

	@InjectMocks
	private FrameReservationServiceHandler reservationServiceHandler;

	private void mockSave() {
		when(reservationRepository.save(any(FrameReservation.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	private void mockActive(FrameReservation reservation) {
		when(reservationRepository.findActiveByApplicantId(1L)).thenReturn(Optional.ofNullable(reservation));
	}

	@Test
	void ValidateCanReserve_Successful() {
		mockActive(null);

		reservationServiceHandler.validateCanReserve(1L, ELIGIBLE, true);
	}

	@Test
	void Reserve_Successful() {
		mockSave();

		FrameReservationResponseDto response = reservationServiceHandler.reserve(1L, 7L);

		assertThat(response.frameId()).isEqualTo(7L);
		assertThat(Duration.between(response.reservedAt(), response.expiresAt())).isEqualTo(Duration.ofDays(7));
	}

	@Test
	void ValidateCanReserve_WhenApplicantAlreadyHasOne() {
		mockActive(new FrameReservation(1L, 3L, 7));

		assertThatThrownBy(() -> reservationServiceHandler.validateCanReserve(1L, ELIGIBLE, true))
				.isInstanceOf(FrameReservationNotAllowedException.class)
				.hasMessageContaining("Ya tenés un marco reservado");
		verify(reservationRepository, never()).save(any());
	}

	@Test
	void ValidateCanReserve_WhenAnsesCertificateIsNotInForce() {
		mockActive(null);
		EligibilityResponseDto notEligible = new EligibilityResponseDto(1L, false, "vencida");

		assertThatThrownBy(() -> reservationServiceHandler.validateCanReserve(1L, notEligible, true))
				.isInstanceOf(FrameReservationNotAllowedException.class)
				.hasMessageContaining("certificación negativa de ANSES vigente");
		verify(reservationRepository, never()).save(any());
	}

	@Test
	void ValidateCanReserve_WhenApplicantHasNoPrescription() {
		mockActive(null);

		assertThatThrownBy(() -> reservationServiceHandler.validateCanReserve(1L, ELIGIBLE, false))
				.isInstanceOf(FrameReservationNotAllowedException.class)
				.hasMessageContaining("receta médica");
		verify(reservationRepository, never()).save(any());
	}

	@Test
	void CancelActive_Successful() {
		FrameReservation reservation = new FrameReservation(1L, 7L, 7);
		mockActive(reservation);
		mockSave();

		reservationServiceHandler.cancelActive(1L);

		assertThat(reservation.getCancelledAt()).isNotNull();
		assertThat(reservation.isActive()).isFalse();
	}

	@Test
	void CancelActive_WhenThereIsNone() {
		mockActive(null);

		assertThatThrownBy(() -> reservationServiceHandler.cancelActive(1L))
				.isInstanceOf(FrameReservationNotFoundException.class);
	}

	@Test
	void CloseForAssignment_WhenAssignedTheReservedFrame() {
		FrameReservation reservation = new FrameReservation(1L, 7L, 7);
		mockActive(reservation);
		mockSave();

		assertThat(reservationServiceHandler.closeForAssignment(1L, 7L)).isEqualTo(7L);
		assertThat(reservation.getFulfilledAt()).isNotNull();
		assertThat(reservation.getCancelledAt()).isNull();
	}

	@Test
	void CloseForAssignment_WhenAssignedAnotherFrame() {
		FrameReservation reservation = new FrameReservation(1L, 7L, 7);
		mockActive(reservation);
		mockSave();

		// Devuelve el reservado para que frames lo libere.
		assertThat(reservationServiceHandler.closeForAssignment(1L, 9L)).isEqualTo(7L);
		assertThat(reservation.getCancelledAt()).isNotNull();
		assertThat(reservation.getFulfilledAt()).isNull();
	}

	@Test
	void CloseForAssignment_WhenThereIsNone() {
		mockActive(null);

		assertThat(reservationServiceHandler.closeForAssignment(1L, 9L)).isNull();
		verify(reservationRepository, never()).save(any());
	}

	@Test
	void ExpireOverdue_ReturnsTheFramesToRelease() {
		FrameReservation overdue = new FrameReservation(1L, 7L, 7);
		when(reservationRepository.findOverdue(any(LocalDateTime.class))).thenReturn(List.of(overdue));

		assertThat(reservationServiceHandler.expireOverdue()).containsExactly(7L);
		assertThat(overdue.getExpiredAt()).isNotNull();
		verify(reservationRepository).saveAll(List.of(overdue));
	}
}
