package com.banco.anteojos.backend.orchestrator.security;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.applicants.ApplicantService;
import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.security.SecurityService;
import com.banco.anteojos.backend.business.security.dto.request.ApplicantRegistrationRequestDto;
import com.banco.anteojos.backend.business.security.dto.request.LoginRequestDto;
import com.banco.anteojos.backend.business.security.dto.response.LoginResponseDto;

import lombok.RequiredArgsConstructor;

// Login no valida pertenencia: es el punto de entrada de la autenticación.
@Component
@RequiredArgsConstructor
public class AuthUseCaseHandler implements AuthUseCaseOrchestrator {

	private final SecurityService securityService;
	private final ApplicantService applicantService;

	@Override
	public LoginResponseDto login(LoginRequestDto request) {
		return securityService.login(request);
	}

	@Override
	@Transactional
	public LoginResponseDto registerApplicant(ApplicantRegistrationRequestDto request) {
		// Coordina dos dominios (applicants + security): el applicant se crea primero porque el
		// login lo referencia por id. Transaccional para no dejar un applicant sin login si el
		// alta del user falla (ej. email duplicado).
		ApplicantResponseDto applicant = applicantService.createApplicant(request.toApplicantCreationRequest());
		String name = request.firstName() + " " + request.lastName();
		return securityService.registerApplicantUser(name, request.email(), request.password(), applicant.id());
	}
}
