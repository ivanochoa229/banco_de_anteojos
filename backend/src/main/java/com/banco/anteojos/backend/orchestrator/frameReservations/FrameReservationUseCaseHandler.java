package com.banco.anteojos.backend.orchestrator.frameReservations;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.applicants.ApplicantService;
import com.banco.anteojos.backend.business.assignments.FrameReservationService;
import com.banco.anteojos.backend.business.assignments.dto.response.FrameReservationDetailResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.FrameReservationResponseDto;
import com.banco.anteojos.backend.business.frames.FrameService;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;

import lombok.RequiredArgsConstructor;

/**
 * Coordina applicants (requisitos para reservar), assignments (la reserva) y frames (el estado del
 * marco). Reservar y liberar mueven las dos cosas a la vez, así que van en una transacción: una
 * reserva registrada nunca deja el marco disponible para otro, ni al revés.
 */
@Component
@RequiredArgsConstructor
public class FrameReservationUseCaseHandler implements FrameReservationUseCaseOrchestrator {

	private final FrameReservationService frameReservationService;
	private final ApplicantService applicantService;
	private final FrameService frameService;

	@Override
	@Transactional
	public FrameReservationDetailResponseDto reserve(Long applicantId, Long frameId) {
		// Pertenencia primero: que el solicitante del token exista.
		applicantService.getApplicant(applicantId);
		frameReservationService.validateCanReserve(applicantId, applicantService.checkEligibility(applicantId),
				applicantService.hasPrescriptions(applicantId));
		// Mover el marco antes de registrar la reserva: es el paso que puede fallar por estado (ya
		// no está disponible), y así ninguna validación falla después de haber escrito algo.
		FrameResponseDto frame = frameService.reserve(frameId);
		FrameReservationResponseDto reservation = frameReservationService.reserve(applicantId, frameId);
		return frameReservationService.buildDetail(reservation, frame);
	}

	@Override
	public FrameReservationDetailResponseDto getReservation(Long applicantId) {
		applicantService.getApplicant(applicantId);
		FrameReservationResponseDto reservation = frameReservationService.getActive(applicantId);
		return frameReservationService.buildDetail(reservation, frameService.getFrame(reservation.frameId()));
	}

	@Override
	@Transactional
	public void cancelReservation(Long applicantId) {
		applicantService.getApplicant(applicantId);
		FrameReservationResponseDto reservation = frameReservationService.cancelActive(applicantId);
		frameService.releaseReservations(List.of(reservation.frameId()));
	}

	@Override
	@Transactional
	public void expireOverdueReservations() {
		frameService.releaseReservations(frameReservationService.expireOverdue());
	}
}
