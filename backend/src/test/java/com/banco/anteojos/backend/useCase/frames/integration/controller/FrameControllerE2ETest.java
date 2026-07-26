package com.banco.anteojos.backend.useCase.frames.integration.controller;

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

import com.banco.anteojos.backend.business.frames.entities.Frame;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;
import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.persistence.frames.FramePostgresSqlRepository;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/frames/frames_template.sql")
class FrameControllerE2ETest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private FramePostgresSqlRepository frameRepository;

	private String bearerToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Operador Test", "operator.test@bancoanteojos.org", "hash", Role.OPERATOR));
	}

	@Test
	void CreateFrame_Successful() throws Exception {
		mockMvc.perform(post("/v1/donors/300000/frames")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"sealCode": "b-2001", "frameType": "SEMI_RIMLESS", "material": "METAL",
								 "lensWidthMm": 54, "bridgeWidthMm": 17, "templeLengthMm": 145}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.donorId").value(300000))
				.andExpect(jsonPath("$.sealCode").value("B-2001"))
				.andExpect(jsonPath("$.status").value("AVAILABLE"));

		assertThat(frameRepository.findBySealCode("B-2001"))
				.hasValueSatisfying(saved -> {
					assertThat(saved.getDonorId()).isEqualTo(300000L);
					assertThat(saved.getStatus()).isEqualTo(FrameStatus.AVAILABLE);
					assertThat(saved.getLensWidthMm()).isEqualTo(54);
				});
	}

	@Test
	void CreateFrame_WhenSealCodeAlreadyExists() throws Exception {
		mockMvc.perform(post("/v1/donors/300000/frames")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"sealCode": "A-1001", "frameType": "FULL_RIM", "material": "ACETATE"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Ya existe un marco con ese precinto"));
	}

	@Test
	void CreateFrame_WhenDonorDoesNotExist() throws Exception {
		mockMvc.perform(post("/v1/donors/999999/frames")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"sealCode": "C-3001", "frameType": "FULL_RIM", "material": "PLASTIC"}
								"""))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Donante no encontrado"));

		assertThat(frameRepository.findBySealCode("C-3001")).isEmpty();
	}

	@Test
	void CreateFrame_WhenMeasurementIsOutOfRange() throws Exception {
		mockMvc.perform(post("/v1/donors/300000/frames")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"sealCode": "C-3002", "frameType": "FULL_RIM", "material": "METAL",
								 "lensWidthMm": 500}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));

		assertThat(frameRepository.findBySealCode("C-3002")).isEmpty();
	}

	@Test
	void ListFrames_WithoutStatusReturnsWholeInventory() throws Exception {
		mockMvc.perform(get("/v1/frames")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void ListFrames_FilteredByStatus() throws Exception {
		mockMvc.perform(get("/v1/frames").param("status", "AVAILABLE")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].sealCode").value("A-1001"));
	}

	@Test
	void ListFramesByDonor_Successful() throws Exception {
		mockMvc.perform(get("/v1/donors/300000/frames")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void ListFramesByDonor_WhenDonorDoesNotExist() throws Exception {
		mockMvc.perform(get("/v1/donors/999999/frames")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Donante no encontrado"));
	}

	@Test
	void GetFrame_Successful() throws Exception {
		mockMvc.perform(get("/v1/frames/400000")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sealCode").value("A-1001"))
				.andExpect(jsonPath("$.frameType").value("FULL_RIM"));
	}

	@Test
	void GetFrame_WhenNotFound() throws Exception {
		mockMvc.perform(get("/v1/frames/999999")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Marco no encontrado"));
	}

	@Test
	void ListFrames_WithoutToken() throws Exception {
		mockMvc.perform(get("/v1/frames"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("No autenticado"));
	}

	@Test
	void SealCodeIsNormalized_SoSameCodeInLowercaseCollides() throws Exception {
		// El precinto A-1001 ya existe en el template: cargarlo en minúscula tiene que chocar.
		assertThat(Frame.normalizeSealCode(" a-1001 ")).isEqualTo("A-1001");

		mockMvc.perform(post("/v1/donors/300000/frames")
						.header(HttpHeaders.AUTHORIZATION, bearerToken())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"sealCode": " a-1001 ", "frameType": "FULL_RIM", "material": "OTHER"}
								"""))
				.andExpect(status().isConflict());
	}
}
