package com.banco.anteojos.backend.business.assignments;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.applicants.dto.response.EligibilityResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.FrameReservationDetailResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.FrameReservationResponseDto;
import com.banco.anteojos.backend.business.assignments.entities.FrameReservation;
import com.banco.anteojos.backend.business.assignments.exception.FrameReservationNotAllowedException;
import com.banco.anteojos.backend.business.assignments.exception.FrameReservationNotFoundException;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.persistence.assignments.FrameReservationPostgresSqlRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FrameReservationServiceHandler implements FrameReservationService {

	// Alcanza para sacar turno y llevar la receta; si no avanza, el marco vuelve al inventario.
	private static final int RESERVATION_DAYS = 7;

	private final FrameReservationPostgresSqlRepository reservationRepository;

	@Override
	public void validateCanReserve(Long applicantId, EligibilityResponseDto eligibility, boolean hasPrescription) {
		// El registro es público: sin estos requisitos cualquiera podría dejar marcos bloqueados.
		if (reservationRepository.findActiveByApplicantId(applicantId).isPresent()) {
			throw new FrameReservationNotAllowedException(
					"Ya tenés un marco reservado. Cancelá esa reserva para elegir otro");
		}
		if (!eligibility.eligible()) {
			throw new FrameReservationNotAllowedException(
					"Para reservar un marco necesitás tu certificación negativa de ANSES vigente. "
							+ "Subila desde Mi Perfil");
		}
		if (!hasPrescription) {
			throw new FrameReservationNotAllowedException(
					"Para reservar un marco primero subí tu receta médica desde Mi Perfil");
		}
	}

	@Override
	public FrameReservationResponseDto reserve(Long applicantId, Long frameId) {
		return toResponse(reservationRepository.save(new FrameReservation(applicantId, frameId, RESERVATION_DAYS)));
	}

	@Override
	public FrameReservationResponseDto getActive(Long applicantId) {
		return toResponse(findActive(applicantId));
	}

	@Override
	public FrameReservationResponseDto cancelActive(Long applicantId) {
		FrameReservation reservation = findActive(applicantId);
		reservation.cancel();
		return toResponse(reservationRepository.save(reservation));
	}

	@Override
	public Long closeForAssignment(Long applicantId, Long assignedFrameId) {
		FrameReservation reservation = reservationRepository.findActiveByApplicantId(applicantId).orElse(null);
		if (reservation == null) {
			return null;
		}
		if (reservation.getFrameId().equals(assignedFrameId)) {
			reservation.fulfill();
		} else {
			reservation.cancel();
		}
		reservationRepository.save(reservation);
		return reservation.getFrameId();
	}

	@Override
	public List<Long> expireOverdue() {
		List<FrameReservation> overdue = reservationRepository.findOverdue(LocalDateTime.now());
		overdue.forEach(FrameReservation::expire);
		reservationRepository.saveAll(overdue);
		return overdue.stream().map(FrameReservation::getFrameId).toList();
	}

	@Override
	public FrameReservationDetailResponseDto buildDetail(FrameReservationResponseDto reservation,
			FrameResponseDto frame) {
		return new FrameReservationDetailResponseDto(reservation, frame);
	}

	private FrameReservation findActive(Long applicantId) {
		return reservationRepository.findActiveByApplicantId(applicantId)
				.orElseThrow(FrameReservationNotFoundException::new);
	}

	private FrameReservationResponseDto toResponse(FrameReservation reservation) {
		return new FrameReservationResponseDto(reservation.getId(), reservation.getApplicantId(),
				reservation.getFrameId(), reservation.getReservedAt(), reservation.getExpiresAt());
	}
}
