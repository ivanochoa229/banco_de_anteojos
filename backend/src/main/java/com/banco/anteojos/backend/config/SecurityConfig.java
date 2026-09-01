package com.banco.anteojos.backend.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.banco.anteojos.backend.presentation.error.ErrorResponseDto;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ObjectMapper objectMapper;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/v1/auth/register").permitAll()
						.requestMatchers("/v1/me/**").hasRole("APPLICANT")
						.requestMatchers("/v1/applicants/**").hasAnyRole("ADMIN", "OPERATOR")
						.requestMatchers("/v1/donors/**").hasAnyRole("ADMIN", "OPERATOR")
						// GET va antes que el matcher genérico de abajo: el solicitante puede navegar
						// el inventario disponible desde el probador virtual, pero no mutarlo.
						.requestMatchers(HttpMethod.GET, "/v1/frames/**").hasAnyRole("ADMIN", "OPERATOR", "APPLICANT")
						.requestMatchers("/v1/frames/**").hasAnyRole("ADMIN", "OPERATOR")
						.requestMatchers("/v1/assignments/**").hasAnyRole("ADMIN", "OPERATOR")
						.requestMatchers("/v1/appointments/**").hasAnyRole("ADMIN", "OPERATOR")
						// Público a propósito: 17TRACK no puede mandar JWT; lo autentica la
						// firma del push (ver ShipmentWebhookController).
						.requestMatchers(HttpMethod.POST, "/v1/shipments/webhook").permitAll()
						.requestMatchers("/v1/shipments/**").hasAnyRole("ADMIN", "OPERATOR")
						.requestMatchers("/v1/indicators/**").hasAnyRole("ADMIN", "OPERATOR")
						.requestMatchers("/v1/catalog/**").hasAnyRole("ADMIN", "OPERATOR")
						.anyRequest().authenticated())
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, e) ->
								writeError(response, request, HttpStatus.UNAUTHORIZED, "No autenticado"))
						.accessDeniedHandler((request, response, e) ->
								writeError(response, request, HttpStatus.FORBIDDEN, "Acceso denegado")))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private void writeError(HttpServletResponse response, HttpServletRequest request,
			HttpStatus status, String message) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(),
				ErrorResponseDto.of(status, message, request.getRequestURI()));
	}
}
