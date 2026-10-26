package com.banco.anteojos.backend.presentation.controllers.applicants;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.assignments.dto.response.FrameReservationDetailResponseDto;
import com.banco.anteojos.backend.orchestrator.frameReservations.FrameReservationUseCaseOrchestrator;

import lombok.RequiredArgsConstructor;

/**
 * El operador ve qué marco eligió el beneficiario en el probador (para asignárselo) y puede
 * liberarlo, por ejemplo si el marco se rompió o la persona ya no va a venir.
 */
@RestController
@RequestMapping("/v1/applicants/{applicantId}/reservation")
@RequiredArgsConstructor
public class ApplicantFrameReservationController {

	private final FrameReservationUseCaseOrchestrator frameReservationOrchestrator;

	/** 404 si no tiene ninguna vigente. */
	@GetMapping
	public FrameReservationDetailResponseDto get(@PathVariable Long applicantId) {
		return frameReservationOrchestrator.getReservation(applicantId);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancel(@PathVariable Long applicantId) {
		frameReservationOrchestrator.cancelReservation(applicantId);
	}
}
