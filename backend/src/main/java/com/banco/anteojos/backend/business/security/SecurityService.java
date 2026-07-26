package com.banco.anteojos.backend.business.security;

import com.banco.anteojos.backend.business.security.dto.request.LoginRequestDto;
import com.banco.anteojos.backend.business.security.dto.response.LoginResponseDto;

public interface SecurityService {

	LoginResponseDto login(LoginRequestDto request);

	void createInitialAdmin(String email, String password);
}
