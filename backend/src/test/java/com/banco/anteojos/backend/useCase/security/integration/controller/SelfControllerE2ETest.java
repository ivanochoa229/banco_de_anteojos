package com.banco.anteojos.backend.useCase.security.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.applicants.entities.Applicant;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentDay;
import com.banco.anteojos.backend.persistence.applicants.ApplicantPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.appointments.AppointmentDayPostgresSqlRepository;
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

	@Autowired
	private AppointmentDayPostgresSqlRepository appointmentDayRepository;

	@Autowired
	private ApplicantPostgresSqlRepository applicantRepository;

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

		Long dayId = appointmentDayRepository
				.save(new AppointmentDay(LocalDate.now().plusDays(3), LocalTime.of(9, 0), 15, 4)).getId();
		mockMvc.perform(post("/v1/me/appointments")
						.header("Authorization", "Bearer " + tokenA)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"appointmentDayId": %d}
								""".formatted(dayId)))
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

	@Test
	void UpdateMyProfile_Successful() throws Exception {
		String tokenA = registerApplicant("30444555", "solicitanteA@mail.com");
		registerApplicant("30555666", "solicitanteB@mail.com");

		// El email que viaje en el body se ignora: es el usuario de ingreso y no se cambia acá.
		mockMvc.perform(put("/v1/me")
						.header("Authorization", "Bearer " + tokenA)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"firstName": "Ana", "lastName": "Gómez", "birthDate": "1990-05-20",
								"phone": "3815551111", "email": "otro@mail.com"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.firstName").value("Ana"))
				.andExpect(jsonPath("$.phone").value("3815551111"))
				.andExpect(jsonPath("$.email").value("solicitantea@mail.com"));

		Applicant updated = applicantRepository.findByDni("30444555").orElseThrow();
		assertThat(updated.getLastName()).isEqualTo("Gómez");
		assertThat(updated.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 20));
		assertThat(updated.getEmail()).isEqualTo("solicitantea@mail.com");

		Applicant other = applicantRepository.findByDni("30555666").orElseThrow();
		assertThat(other.getFirstName()).isEqualTo("Solicitante");
		assertThat(other.getPhone()).isNull();
	}

	@Test
	void UpdateMyProfile_WhenNameIsBlank() throws Exception {
		String token = registerApplicant("30444555", "solicitanteA@mail.com");

		mockMvc.perform(put("/v1/me")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"firstName": " ", "lastName": "Gómez"}
								"""))
				.andExpect(status().isBadRequest());
	}
}
