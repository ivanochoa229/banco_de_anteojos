package com.banco.anteojos.backend.presentation.controllers.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.security.dto.request.LoginRequestDto;
import com.banco.anteojos.backend.business.security.dto.response.LoginResponseDto;
import com.banco.anteojos.backend.orchestrator.security.AuthUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthUseCaseOrchestrator authOrchestrator;

	@PostMapping("/login")
	public LoginResponseDto login(@Valid @RequestBody LoginRequestDto request) {
		return authOrchestrator.login(request);
	}
}
