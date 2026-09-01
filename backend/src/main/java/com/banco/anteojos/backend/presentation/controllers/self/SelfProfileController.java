package com.banco.anteojos.backend.presentation.controllers.self;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.security.CurrentUserService;
import com.banco.anteojos.backend.orchestrator.applicants.ApplicantUseCaseOrchestrator;

import lombok.RequiredArgsConstructor;

// El applicantId nunca viene del cliente: sale del JWT vía CurrentUserService.
@RestController
@RequestMapping("/v1/me")
@RequiredArgsConstructor
public class SelfProfileController {

	private final ApplicantUseCaseOrchestrator applicantOrchestrator;
	private final CurrentUserService currentUserService;

	@GetMapping
	public ApplicantResponseDto profile() {
		return applicantOrchestrator.getApplicant(currentUserService.getApplicantId());
	}
}
