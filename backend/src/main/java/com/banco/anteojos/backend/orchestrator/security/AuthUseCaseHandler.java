package com.banco.anteojos.backend.orchestrator.security;

import org.springframework.stereotype.Component;

import com.banco.anteojos.backend.business.security.SecurityService;
import com.banco.anteojos.backend.business.security.dto.request.LoginRequestDto;
import com.banco.anteojos.backend.business.security.dto.response.LoginResponseDto;

import lombok.RequiredArgsConstructor;

// Login no valida pertenencia: es el punto de entrada de la autenticación.
@Component
@RequiredArgsConstructor
public class AuthUseCaseHandler implements AuthUseCaseOrchestrator {

	private final SecurityService securityService;

	@Override
	public LoginResponseDto login(LoginRequestDto request) {
		return securityService.login(request);
	}
}
