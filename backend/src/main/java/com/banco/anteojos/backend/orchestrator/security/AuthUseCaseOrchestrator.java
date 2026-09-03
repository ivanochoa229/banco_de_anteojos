package com.banco.anteojos.backend.orchestrator.security;

import com.banco.anteojos.backend.business.security.dto.request.ApplicantRegistrationRequestDto;
import com.banco.anteojos.backend.business.security.dto.request.LoginRequestDto;
import com.banco.anteojos.backend.business.security.dto.response.LoginResponseDto;

public interface AuthUseCaseOrchestrator {

	LoginResponseDto login(LoginRequestDto request);

	LoginResponseDto registerApplicant(ApplicantRegistrationRequestDto request);
}
