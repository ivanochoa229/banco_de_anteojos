package com.banco.anteojos.backend.useCase.security.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.SecurityServiceHandler;
import com.banco.anteojos.backend.business.security.dto.request.LoginRequestDto;
import com.banco.anteojos.backend.business.security.dto.response.LoginResponseDto;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.business.security.exception.EmailAlreadyExistsException;
import com.banco.anteojos.backend.business.security.exception.InvalidCredentialsException;
import com.banco.anteojos.backend.persistence.security.UserPostgresSqlRepository;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class SecurityServiceHandlerTest {

	@Mock
	private UserPostgresSqlRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@InjectMocks
	private SecurityServiceHandler securityServiceHandler;

	private User user;

	@BeforeEach
	void setUp() {
		user = new User("Test", "test@mail.com", "hash", Role.OPERATOR);
	}

	@Test
	void Login_Successful() {
		when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password123", "hash")).thenReturn(true);
		when(jwtService.generateToken(user)).thenReturn("jwt-token");

		LoginResponseDto response = securityServiceHandler.login(new LoginRequestDto("test@mail.com", "password123"));

		assertThat(response.token()).isEqualTo("jwt-token");
	}

	@Test
	void Login_WhenEmailDoesNotExist() {
		when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> securityServiceHandler.login(new LoginRequestDto("test@mail.com", "password123")))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void Login_WhenPasswordIsWrong() {
		when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong-password", "hash")).thenReturn(false);

		assertThatThrownBy(() -> securityServiceHandler.login(new LoginRequestDto("test@mail.com", "wrong-password")))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void Login_WhenUserIsInactive() {
		user.deactivate();
		when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password123", "hash")).thenReturn(true);

		assertThatThrownBy(() -> securityServiceHandler.login(new LoginRequestDto("test@mail.com", "password123")))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void CreateInitialAdmin_WhenNoUsersExist() {
		when(userRepository.count()).thenReturn(0L);
		when(passwordEncoder.encode("secret")).thenReturn("encoded-hash");

		securityServiceHandler.createInitialAdmin("Admin@Mail.com", "secret");

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		User saved = captor.getValue();
		assertThat(saved.getEmail()).isEqualTo("admin@mail.com");
		assertThat(saved.getPasswordHash()).isEqualTo("encoded-hash");
		assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
		assertThat(saved.isActive()).isTrue();
	}

	@Test
	void CreateInitialAdmin_WhenUsersAlreadyExist() {
		when(userRepository.count()).thenReturn(3L);

		securityServiceHandler.createInitialAdmin("admin@mail.com", "secret");

		verify(userRepository, never()).save(any());
	}

	@Test
	void RegisterApplicantUser_Successful() {
		when(userRepository.findByEmail("nueva@mail.com")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("password123")).thenReturn("encoded-hash");
		when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(jwtService.generateToken(any())).thenReturn("jwt-token");

		LoginResponseDto response = securityServiceHandler.registerApplicantUser(
				"Nueva Beneficiaria", "Nueva@Mail.com", "password123", 55L);

		assertThat(response.token()).isEqualTo("jwt-token");
		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		User saved = captor.getValue();
		assertThat(saved.getEmail()).isEqualTo("nueva@mail.com");
		assertThat(saved.getRole()).isEqualTo(Role.APPLICANT);
		assertThat(saved.getApplicantId()).isEqualTo(55L);
	}

	@Test
	void RegisterApplicantUser_WhenEmailAlreadyExists() {
		when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> securityServiceHandler.registerApplicantUser(
				"Otra Persona", "test@mail.com", "password123", 55L))
				.isInstanceOf(EmailAlreadyExistsException.class);
		verify(userRepository, never()).save(any());
	}
}
