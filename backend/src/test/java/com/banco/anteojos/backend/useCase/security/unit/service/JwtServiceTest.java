package com.banco.anteojos.backend.useCase.security.unit.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;

@Tag("unit")
class JwtServiceTest {

	private static final String SECRET = "test-secret-key-for-hs256-that-is-long-enough-1234567890";

	private final JwtService jwtService = new JwtService(SECRET, 60_000);
	private final User user = new User("Test", "test@mail.com", "hash", Role.ADMIN);

	@Test
	void GenerateToken_AndValidate_Successful() {
		String token = jwtService.generateToken(user);

		assertThat(jwtService.validate(token)).hasValueSatisfying(tokenData -> {
			assertThat(tokenData.email()).isEqualTo("test@mail.com");
			assertThat(tokenData.role()).isEqualTo("ADMIN");
		});
	}

	@Test
	void Validate_WhenTokenExpired() {
		JwtService expiredIssuer = new JwtService(SECRET, -1_000);
		String token = expiredIssuer.generateToken(user);

		assertThat(expiredIssuer.validate(token)).isEmpty();
	}

	@Test
	void Validate_WhenSignatureIsInvalid() {
		JwtService otherIssuer = new JwtService(SECRET + "-other", 60_000);
		String token = otherIssuer.generateToken(user);

		assertThat(jwtService.validate(token)).isEmpty();
	}

	@Test
	void Validate_WhenTokenIsMalformed() {
		assertThat(jwtService.validate("not-a-jwt")).isEmpty();
	}
}
