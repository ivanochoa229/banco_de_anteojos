package com.banco.anteojos.backend.useCase.security.unit.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banco.anteojos.backend.business.security.SecurityService;
import com.banco.anteojos.backend.config.AdminBootstrap;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

	@Mock
	private SecurityService securityService;

	@Test
	void Run_Successful() {
		new AdminBootstrap(securityService, "admin@test.org", "admin-pass", "operador@test.org", "operador-pass")
				.run();

		verify(securityService).createInitialAdmin("admin@test.org", "admin-pass");
		verify(securityService).createInitialOperator("operador@test.org", "operador-pass");
	}

	@Test
	void Run_WhenOperatorPasswordIsBlank() {
		new AdminBootstrap(securityService, "admin@test.org", "admin-pass", "operador@test.org", "").run();

		verify(securityService).createInitialAdmin("admin@test.org", "admin-pass");
		verify(securityService, never()).createInitialOperator(anyString(), anyString());
	}

	@Test
	void Run_WhenAdminIsNotConfigured() {
		new AdminBootstrap(securityService, "", "", "operador@test.org", "operador-pass").run();

		verifyNoInteractions(securityService);
	}
}
