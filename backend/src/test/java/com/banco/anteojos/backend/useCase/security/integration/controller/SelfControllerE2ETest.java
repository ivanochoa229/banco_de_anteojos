package com.banco.anteojos.backend.useCase.security.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

// Verifica que /v1/me/** aísla por token: nunca por un id que mande el cliente. El caso que
// importa acá no es "applicantId inválido" (no existe ese parámetro), sino que el JWT de un
// solicitante jamás pueda ver el turno de otro.
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/security/users_template.sql")
class SelfControllerE2ETest {

	@Autowired
	private MockMvc mockMvc;

	private String registerApplicant(String dni, String email) throws Exception {
		String body = mockMvc.perform(post("/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"firstName": "Solicitante", "lastName": "Test", "dni": "%s",
								"email": "%s", "password": "password123"}
								""".formatted(dni, email)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.token");
	}

	@Test
	void MyAppointments_OnlyReturnsOwnAppointments() throws Exception {
		String tokenA = registerApplicant("30444555", "solicitanteA@mail.com");
		String tokenB = registerApplicant("30555666", "solicitanteB@mail.com");

		String scheduledAt = LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		mockMvc.perform(post("/v1/me/appointments")
						.header("Authorization", "Bearer " + tokenA)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"scheduledAt": "%s"}
								""".formatted(scheduledAt)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/v1/me/appointments").header("Authorization", "Bearer " + tokenA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		mockMvc.perform(get("/v1/me/appointments").header("Authorization", "Bearer " + tokenB))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void MyAppointments_WhenNoToken() throws Exception {
		mockMvc.perform(get("/v1/me/appointments")).andExpect(status().isUnauthorized());
	}

	@Test
	void MyAppointments_WhenStaffToken() throws Exception {
		String body = mockMvc.perform(post("/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "operator.test@bancoanteojos.org", "password": "password123"}
								"""))
				.andReturn().getResponse().getContentAsString();
		String operatorToken = JsonPath.read(body, "$.token");

		mockMvc.perform(get("/v1/me/appointments").header("Authorization", "Bearer " + operatorToken))
				.andExpect(status().isForbidden());
	}
}
