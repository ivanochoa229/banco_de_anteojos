package com.banco.anteojos.backend.useCase.security.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.persistence.applicants.ApplicantPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.applicants.PrescriptionPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;
import com.jayway.jsonpath.JsonPath;

// El beneficiario sube la receta del médico desde su portal. El token decide de quién es: nunca
// queda cargada a otro, ni se puede leer el archivo de una receta ajena.
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SelfPrescriptionE2ETest {

	private static final String DNI_A = "30444555";
	private static final String DNI_B = "30555666";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ApplicantPostgresSqlRepository applicantRepository;

	@Autowired
	private PrescriptionPostgresSqlRepository prescriptionRepository;

	// Único mock del test: no se le pega a R2 de verdad en integración.
	@MockitoBean
	private R2StorageClient r2StorageClient;

	@BeforeEach
	void stubPresignedUrl() {
		when(r2StorageClient.presignedGetUrl(any()))
				.thenReturn(new PresignedUrl("https://r2.example/firmada", LocalDateTime.now().plusMinutes(15)));
	}

	private String registerApplicant(String dni, String email) throws Exception {
		String body = mockMvc.perform(post("/v1/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"firstName": "Solicitante", "lastName": "Test", "dni": "%s",
								"email": "%s", "password": "password123"}
								""".formatted(dni, email)))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return "Bearer " + JsonPath.read(body, "$.token");
	}

	private MockMultipartFile prescriptionPdf() {
		return new MockMultipartFile("file", "receta.pdf", "application/pdf", "%PDF-1.4".getBytes());
	}

	private Long uploadPrescription(String token) throws Exception {
		String body = mockMvc.perform(multipart("/v1/me/prescriptions").file(prescriptionPdf())
						.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(body, "$.id")).longValue();
	}

	@Test
	void UploadMyPrescription_Successful() throws Exception {
		String tokenA = registerApplicant(DNI_A, "solicitanteA@mail.com");
		registerApplicant(DNI_B, "solicitanteB@mail.com");

		mockMvc.perform(multipart("/v1/me/prescriptions").file(prescriptionPdf())
						.header(HttpHeaders.AUTHORIZATION, tokenA))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.fileOriginalName").value("receta.pdf"))
				.andExpect(jsonPath("$.rightSphere").isEmpty());

		Long applicantA = applicantRepository.findByDni(DNI_A).orElseThrow().getId();
		Long applicantB = applicantRepository.findByDni(DNI_B).orElseThrow().getId();
		assertThat(prescriptionRepository.findByApplicantId(applicantA))
				.singleElement().satisfies(prescription -> assertThat(prescription.hasFile()).isTrue());
		assertThat(prescriptionRepository.findByApplicantId(applicantB)).isEmpty();

		mockMvc.perform(get("/v1/me/prescriptions").header(HttpHeaders.AUTHORIZATION, tokenA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void UploadMyPrescription_WhenFileTypeNotAllowed() throws Exception {
		String token = registerApplicant(DNI_A, "solicitanteA@mail.com");

		mockMvc.perform(multipart("/v1/me/prescriptions")
						.file(new MockMultipartFile("file", "receta.txt", "text/plain", "hola".getBytes()))
						.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isBadRequest());

		Long applicantId = applicantRepository.findByDni(DNI_A).orElseThrow().getId();
		assertThat(prescriptionRepository.findByApplicantId(applicantId)).isEmpty();
	}

	@Test
	void GetMyPrescriptionFile_Successful() throws Exception {
		String token = registerApplicant(DNI_A, "solicitanteA@mail.com");
		Long prescriptionId = uploadPrescription(token);

		mockMvc.perform(get("/v1/me/prescriptions/" + prescriptionId + "/file")
						.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value("https://r2.example/firmada"));
	}

	@Test
	void GetMyPrescriptionFile_WhenPrescriptionBelongsToAnotherApplicant() throws Exception {
		String tokenA = registerApplicant(DNI_A, "solicitanteA@mail.com");
		String tokenB = registerApplicant(DNI_B, "solicitanteB@mail.com");
		Long prescriptionOfA = uploadPrescription(tokenA);

		mockMvc.perform(get("/v1/me/prescriptions/" + prescriptionOfA + "/file")
						.header(HttpHeaders.AUTHORIZATION, tokenB))
				.andExpect(status().isNotFound());
	}
}
