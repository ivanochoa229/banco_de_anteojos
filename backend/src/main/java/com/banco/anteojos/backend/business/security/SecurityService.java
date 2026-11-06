package com.banco.anteojos.backend.business.security;

import java.util.Collection;
import java.util.Map;

import com.banco.anteojos.backend.business.security.dto.request.LoginRequestDto;
import com.banco.anteojos.backend.business.security.dto.response.LoginResponseDto;

public interface SecurityService {

	LoginResponseDto login(LoginRequestDto request);

	void createInitialAdmin(String email, String password);

	void createInitialOperator(String email, String password);

	/** Nombres de los usuarios por id, para mostrar quién fue el responsable de cada acción. */
	Map<Long, String> getUserNames(Collection<Long> userIds);

	/** Crea el login del solicitante y devuelve el token, igual que login: registro = alta + sesión. */
	LoginResponseDto registerApplicantUser(String name, String email, String rawPassword, Long applicantId);
}
