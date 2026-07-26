package com.banco.anteojos.backend.useCase.donors.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.banco.anteojos.backend.business.donors.entities.DonorType;
import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.persistence.donors.DonorPostgresSqlRepository;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/donors/donors_template.sql")
class DonorControllerE2ETest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private DonorPostgresSqlRepository donorRepository;

	private String bearerToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Operador Test", "operator.test@bancoanteojos.org", "hash", Role.OPERATOR));
	}

	@Test
	void CreateDonor_Successful() throws Exception {
		mockMvc.perform(post("/v1/donors")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"donorType": "INDIVIDUAL", "name": "Ana Ruiz", "documentNumber": "28444555",
								 "phone": "3815551111", "email": "ana.ruiz@mail.com"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.donorType").value("INDIVIDUAL"))
				.andExpect(jsonPath("$.name").value("Ana Ruiz"));

		assertThat(donorRepository.findAllByOrderByNameAsc())
				.anySatisfy(donor -> {
					assertThat(donor.getName()).isEqualTo("Ana Ruiz");
					assertThat(donor.getDonorType()).isEqualTo(DonorType.INDIVIDUAL);
				});
	}

	@Test
	void CreateDonor_WhenDonorTypeIsInvalid() throws Exception {
		mockMvc.perform(post("/v1/donors")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"donorType": "EMPRESA", "name": "Alguien"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void CreateDonor_WhenNameIsMissing() throws Exception {
		mockMvc.perform(post("/v1/donors")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"donorType": "INDIVIDUAL"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("name: must not be blank"));
	}

	@Test
	void ListDonors_Successful() throws Exception {
		mockMvc.perform(get("/v1/donors")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].name").value("Rotary Club Tucumán"));
	}

	@Test
	void GetDonor_Successful() throws Exception {
		mockMvc.perform(get("/v1/donors/300000")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.documentNumber").value("30711111119"));
	}

	@Test
	void GetDonor_WhenNotFound() throws Exception {
		mockMvc.perform(get("/v1/donors/999999")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Donante no encontrado"));
	}

	@Test
	void ListDonors_WithoutToken() throws Exception {
		mockMvc.perform(get("/v1/donors"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("No autenticado"));
	}
}
