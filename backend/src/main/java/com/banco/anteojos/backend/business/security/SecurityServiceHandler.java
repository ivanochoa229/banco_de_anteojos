package com.banco.anteojos.backend.business.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.security.dto.request.LoginRequestDto;
import com.banco.anteojos.backend.business.security.dto.response.LoginResponseDto;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.business.security.exception.EmailAlreadyExistsException;
import com.banco.anteojos.backend.business.security.exception.InvalidCredentialsException;
import com.banco.anteojos.backend.persistence.security.UserPostgresSqlRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityServiceHandler implements SecurityService {

	private static final String INITIAL_ADMIN_NAME = "Administrador";

	private final UserPostgresSqlRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Override
	public LoginResponseDto login(LoginRequestDto request) {
		User user = userRepository.findByEmail(request.email().trim().toLowerCase())
				.filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
				.filter(User::isActive)
				.orElseThrow(InvalidCredentialsException::new);
		return new LoginResponseDto(jwtService.generateToken(user));
	}

	@Override
	public void createInitialAdmin(String email, String password) {
		if (userRepository.count() > 0) {
			return;
		}
		userRepository.save(new User(INITIAL_ADMIN_NAME, email, passwordEncoder.encode(password), Role.ADMIN));
		log.info("Admin inicial creado con email {}", email);
	}

	@Override
	public void createInitialOperator(String email, String password) {
		String normalizedEmail = email.trim().toLowerCase();
		User operator = userRepository.findByEmail(normalizedEmail)
				.orElseGet(() -> new User("Operador Hacer Futuro", normalizedEmail, passwordEncoder.encode(password), Role.OPERATOR));
		operator.changePassword(passwordEncoder.encode(password));
		operator.activate();
		userRepository.save(operator);
		log.info("Operador inicial asegurado con email {}", normalizedEmail);
	}

	@Override
	public LoginResponseDto registerApplicantUser(String name, String email, String rawPassword, Long applicantId) {
		String normalizedEmail = email.trim().toLowerCase();
		if (userRepository.findByEmail(normalizedEmail).isPresent()) {
			throw new EmailAlreadyExistsException();
		}
		User user = userRepository.save(new User(name, normalizedEmail, passwordEncoder.encode(rawPassword),
				Role.APPLICANT, applicantId));
		return new LoginResponseDto(jwtService.generateToken(user));
	}
}
