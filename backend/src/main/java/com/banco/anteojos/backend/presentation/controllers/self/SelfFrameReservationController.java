package com.banco.anteojos.backend.presentation.controllers.self;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.assignments.dto.response.FrameReservationDetailResponseDto;
import com.banco.anteojos.backend.business.security.CurrentUserService;
import com.banco.anteojos.backend.orchestrator.frameReservations.FrameReservationUseCaseOrchestrator;

import lombok.RequiredArgsConstructor;

/**
 * El beneficiario reserva desde el probador el marco que le gustó. Hay una sola reserva vigente
 * por persona, por eso es un sub-recurso singular ({@code /reservation}); el marco va en el path
 * al crearla. El applicantId sale del JWT.
 */
@RestController
@RequestMapping("/v1/me")
@RequiredArgsConstructor
public class SelfFrameReservationController {

	private final FrameReservationUseCaseOrchestrator frameReservationOrchestrator;
	private final CurrentUserService currentUserService;

	@PostMapping("/frames/{frameId}/reservation")
	@ResponseStatus(HttpStatus.CREATED)
	public FrameReservationDetailResponseDto reserve(@PathVariable Long frameId) {
		return frameReservationOrchestrator.reserve(currentUserService.getApplicantId(), frameId);
	}

	/** 404 si no tiene ninguna vigente. */
	@GetMapping("/reservation")
	public FrameReservationDetailResponseDto get() {
		return frameReservationOrchestrator.getReservation(currentUserService.getApplicantId());
	}

	@DeleteMapping("/reservation")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancel() {
		frameReservationOrchestrator.cancelReservation(currentUserService.getApplicantId());
	}
}
