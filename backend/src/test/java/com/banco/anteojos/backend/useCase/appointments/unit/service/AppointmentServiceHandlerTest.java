package com.banco.anteojos.backend.useCase.appointments.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banco.anteojos.backend.business.appointments.AppointmentServiceHandler;
import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentCreationRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentResponseDto;
import com.banco.anteojos.backend.business.appointments.entities.Appointment;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus;
import com.banco.anteojos.backend.business.appointments.exception.AppointmentNotFoundException;
import com.banco.anteojos.backend.persistence.appointments.AppointmentPostgresSqlRepository;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AppointmentServiceHandlerTest {

	private static final LocalDateTime SCHEDULED_AT = LocalDateTime.now().plusDays(3);

	@Mock
	private AppointmentPostgresSqlRepository appointmentRepository;

	@InjectMocks
	private AppointmentServiceHandler appointmentServiceHandler;

	private Appointment appointment() {
		return new Appointment(1L, null, SCHEDULED_AT, null);
	}

	private void mockFind(Appointment appointment) {
		when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
		when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	void CreateAppointment_Successful() {
		when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

		AppointmentResponseDto response = appointmentServiceHandler.createAppointment(1L,
				new AppointmentCreationRequestDto(SCHEDULED_AT, 9L, "retiro de anteojos"));

		assertThat(response.applicantId()).isEqualTo(1L);
		assertThat(response.assignmentId()).isEqualTo(9L);
		assertThat(response.scheduledAt()).isEqualTo(SCHEDULED_AT);
		assertThat(response.status()).isEqualTo("SCHEDULED");
		assertThat(response.notes()).isEqualTo("retiro de anteojos");
		assertThat(response.createdAt()).isNotNull();
	}

	@Test
	void GetAppointment_WhenNotFound() {
		when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> appointmentServiceHandler.getAppointment(999L))
				.isInstanceOf(AppointmentNotFoundException.class);
	}

	@Test
	void Reschedule_Successful() {
		mockFind(appointment());
		LocalDateTime newDate = SCHEDULED_AT.plusDays(2);

		AppointmentResponseDto response = appointmentServiceHandler.reschedule(10L, newDate);

		assertThat(response.scheduledAt()).isEqualTo(newDate);
		assertThat(response.status()).isEqualTo("SCHEDULED");
		verify(appointmentRepository).save(any(Appointment.class));
	}

	@Test
	void Cancel_Successful() {
		mockFind(appointment());

		AppointmentResponseDto response = appointmentServiceHandler.cancel(10L, "viaja esa semana");

		assertThat(response.status()).isEqualTo("CANCELLED");
		assertThat(response.cancellationReason()).isEqualTo("viaja esa semana");
	}

	@Test
	void RegisterAttendance_WhenAttended() {
		mockFind(appointment());

		assertThat(appointmentServiceHandler.registerAttendance(10L, true).status()).isEqualTo("COMPLETED");
	}

	@Test
	void RegisterAttendance_WhenMissed() {
		mockFind(appointment());

		assertThat(appointmentServiceHandler.registerAttendance(10L, false).status()).isEqualTo("MISSED");
	}

	@Test
	void ListAppointments_WhenDateIsGiven() {
		LocalDate date = LocalDate.now().plusDays(3);
		when(appointmentRepository.findByDay(date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
				.thenReturn(List.of(appointment()));

		assertThat(appointmentServiceHandler.listAppointments(date)).hasSize(1);
		verify(appointmentRepository).findByDay(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
	}

	@Test
	void ListAppointments_WhenAll() {
		when(appointmentRepository.findAllByOrderByScheduledAtDesc()).thenReturn(List.of(appointment()));

		assertThat(appointmentServiceHandler.listAppointments(null)).hasSize(1);
	}

	@Test
	void ListAppointmentsByApplicant_Successful() {
		when(appointmentRepository.findByApplicantIdOrderByScheduledAtDesc(1L))
				.thenReturn(List.of(appointment()));

		assertThat(appointmentServiceHandler.listAppointmentsByApplicant(1L)).hasSize(1);
	}

	@Test
	void CountByStatus_DelegatesToRepository() {
		LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(2026, 2, 1, 0, 0);
		when(appointmentRepository.countByStatusAndScheduledAtBetween(AppointmentStatus.COMPLETED, from, to))
				.thenReturn(6L);

		assertThat(appointmentServiceHandler.countByStatus(AppointmentStatus.COMPLETED, from, to)).isEqualTo(6L);
	}
}
