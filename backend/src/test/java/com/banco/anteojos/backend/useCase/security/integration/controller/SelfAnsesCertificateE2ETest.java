package com.banco.anteojos.backend.useCase.security.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.applicants.entities.Applicant;
import com.banco.anteojos.backend.persistence.applicants.AnsesCertificatePostgresSqlRepository;
import com.banco.anteojos.backend.persistence.applicants.ApplicantPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;
import com.banco.anteojos.backend.useCase.applicants.AnsesCertificatePdfFactory;
import com.jayway.jsonpath.JsonPath;

// El beneficiario sube su propia negativa: la validación es la misma que la del operador, y el
// token decide a qué solicitante se le carga. Lo que importa es que nunca quede en otro.
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SelfAnsesCertificateE2ETest {

	private static final String DNI_A = "30444555";
	private static final String DNI_B = "30555666";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ApplicantPostgresSqlRepository applicantRepository;

	@Autowired
	private AnsesCertificatePostgresSqlRepository ansesCertificateRepository;

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

	private MockMultipartFile pdf(byte[] content) {
		return new MockMultipartFile("file", "negativa.pdf", "application/pdf", content);
	}

	@Test
	void UploadMyAnsesCertificate_Successful() throws Exception {
		String tokenA = registerApplicant(DNI_A, "solicitanteA@mail.com");
		registerApplicant(DNI_B, "solicitanteB@mail.com");
		String cuilA = AnsesCertificatePdfFactory.cuilFor(DNI_A);

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/me/anses-certificate")
						.file(pdf(AnsesCertificatePdfFactory.certificate(cuilA, "221145098", LocalDate.now())))
						.header(HttpHeaders.AUTHORIZATION, tokenA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cuil").value(cuilA));

		Applicant applicantA = applicantRepository.findByDni(DNI_A).orElseThrow();
		assertThat(applicantA.getCuil()).isEqualTo(cuilA);
		assertThat(ansesCertificateRepository.findByApplicantId(applicantA.getId())).isPresent();

		Applicant applicantB = applicantRepository.findByDni(DNI_B).orElseThrow();
		assertThat(applicantB.getCuil()).isNull();
		assertThat(ansesCertificateRepository.findByApplicantId(applicantB.getId())).isEmpty();

		mockMvc.perform(get("/v1/me").header(HttpHeaders.AUTHORIZATION, tokenA))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cuil").value(cuilA));
	}

	@Test
	void UploadMyAnsesCertificate_WhenCuilBelongsToAnotherPerson() throws Exception {
		String tokenA = registerApplicant(DNI_A, "solicitanteA@mail.com");
		String cuilB = AnsesCertificatePdfFactory.cuilFor(DNI_B);

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/me/anses-certificate")
						.file(pdf(AnsesCertificatePdfFactory.certificate(cuilB, "221145098", LocalDate.now())))
						.header(HttpHeaders.AUTHORIZATION, tokenA))
				.andExpect(status().isUnprocessableContent());

		Applicant applicantA = applicantRepository.findByDni(DNI_A).orElseThrow();
		assertThat(applicantA.getCuil()).isNull();
		assertThat(ansesCertificateRepository.findByApplicantId(applicantA.getId())).isEmpty();
	}

	@Test
	void GetMyAnsesCertificate_WhenNotUploaded() throws Exception {
		String token = registerApplicant(DNI_A, "solicitanteA@mail.com");

		mockMvc.perform(get("/v1/me/anses-certificate").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isNotFound());
	}
}
