package com.banco.anteojos.backend.useCase.applicants.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.applicants.entities.Applicant;
import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.persistence.applicants.ApplicantPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.applicants.PrescriptionPostgresSqlRepository;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/applicants/applicants_template.sql")
class ApplicantControllerE2ETest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private ApplicantPostgresSqlRepository applicantRepository;

	@Autowired
	private PrescriptionPostgresSqlRepository prescriptionRepository;

	// El filtro JWT solo valida la firma del token, no consulta la DB: no hace falta un usuario persistido.
	private String bearerToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Operador Test", "operator.test@bancoanteojos.org", "hash", Role.OPERATOR));
	}

	@Test
	void CreateApplicant_Successful() throws Exception {
		mockMvc.perform(post("/v1/applicants")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"firstName": "Carlos", "lastName": "Gómez", "dni": "40111222",
								 "birthDate": "1990-07-01", "phone": "3815552222", "email": "carlos@mail.com"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.dni").value("40111222"))
				.andExpect(jsonPath("$.identityValidated").value(false));

		assertThat(applicantRepository.findByDni("40111222"))
				.hasValueSatisfying(saved -> assertThat(saved.getFirstName()).isEqualTo("Carlos"));
	}

	@Test
	void CreateApplicant_WhenDniAlreadyExists() throws Exception {
		mockMvc.perform(post("/v1/applicants")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"firstName": "Otra", "lastName": "Persona", "dni": "30123456"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.message").value("Ya existe un solicitante con ese DNI"));
	}

	@Test
	void ListApplicants_Successful() throws Exception {
		mockMvc.perform(get("/v1/applicants")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].dni").value("30123456"));
	}

	@Test
	void UpdateApplicant_Successful() throws Exception {
		mockMvc.perform(put("/v1/applicants/200000")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"firstName": "Juana María", "lastName": "Pérez",
								 "birthDate": "1985-03-12", "phone": "3815559999", "email": "juana.nueva@mail.com"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.firstName").value("Juana María"))
				.andExpect(jsonPath("$.dni").value("30123456"));

		Applicant updated = applicantRepository.findById(200000L).orElseThrow();
		assertThat(updated.getFirstName()).isEqualTo("Juana María");
		assertThat(updated.getPhone()).isEqualTo("3815559999");
	}

	@Test
	void UpdateApplicant_WhenNotFound() throws Exception {
		mockMvc.perform(put("/v1/applicants/999999")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"firstName": "Nadie", "lastName": "Nadie"}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Solicitante no encontrado"));
	}

	@Test
	void AddPrescription_AndListIt_Successful() throws Exception {
		mockMvc.perform(post("/v1/applicants/200000/prescriptions")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"rightSphere": -1.25, "rightCylinder": -0.50, "rightAxis": 90,
								 "leftSphere": -1.00, "leftCylinder": null, "leftAxis": null}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.applicantId").value(200000))
				.andExpect(jsonPath("$.rightAxis").value(90));

		assertThat(prescriptionRepository.findByApplicantId(200000L)).hasSize(1);

		mockMvc.perform(get("/v1/applicants/200000/prescriptions")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].rightSphere").value(-1.25));
	}

	@Test
	void ListApplicants_WithoutToken() throws Exception {
		mockMvc.perform(get("/v1/applicants"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("No autenticado"));
	}
}
