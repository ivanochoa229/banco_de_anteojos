package com.banco.anteojos.backend.useCase.applicants.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.applicants.entities.AnsesCertificate;
import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.persistence.applicants.AnsesCertificatePostgresSqlRepository;
import com.banco.anteojos.backend.persistence.applicants.ApplicantPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;
import com.banco.anteojos.backend.useCase.applicants.AnsesCertificatePdfFactory;

// El applicant 200000 del template tiene DNI 30123456; su CUIL con prefijo 20 es 20-30123456-3.
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/applicants/applicants_template.sql")
class ApplicantAnsesCertificateE2ETest {

	private static final String MATCHING_CUIL = "20301234563";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

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

	private String bearerToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Operador Test", "operator.test@bancoanteojos.org", "hash", Role.OPERATOR));
	}

	private MockMultipartFile pdf(byte[] content) {
		return new MockMultipartFile("file", "negativa.pdf", "application/pdf", content);
	}

	@Test
	void UploadAnsesCertificate_Successful() throws Exception {
		byte[] pdf = AnsesCertificatePdfFactory.certificate(MATCHING_CUIL, "221145098", LocalDate.now());

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/applicants/200000/anses-certificate")
						.file(pdf(pdf))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cuil").value(MATCHING_CUIL))
				.andExpect(jsonPath("$.transactionNumber").value("221145098"))
				.andExpect(jsonPath("$.issueDate").value(LocalDate.now().toString()))
				.andExpect(jsonPath("$.fileUrl").value("https://r2.example/firmada"));

		AnsesCertificate saved = ansesCertificateRepository.findByApplicantId(200000L).orElseThrow();
		assertThat(saved.getFileKey()).startsWith("anses/200000/").endsWith(".pdf");
		verify(r2StorageClient).upload(any(), any(), any());
		// La negativa validada confirma el CUIL del solicitante.
		assertThat(applicantRepository.findById(200000L).orElseThrow().getCuil()).isEqualTo(MATCHING_CUIL);
	}

	@Test
	void UploadAnsesCertificate_WhenCuilBelongsToAnotherPerson() throws Exception {
		byte[] pdf = AnsesCertificatePdfFactory.certificate("20999999981", "221145098", LocalDate.now());

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/applicants/200000/anses-certificate")
						.file(pdf(pdf))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.message").value(
						"La certificación pertenece a otra persona: el CUIL no corresponde al DNI del solicitante"));

		assertThat(ansesCertificateRepository.findByApplicantId(200000L)).isEmpty();
		verify(r2StorageClient, never()).upload(any(), any(), any());
	}

	@Test
	void UploadAnsesCertificate_WhenCertificateIsExpired() throws Exception {
		byte[] pdf = AnsesCertificatePdfFactory.certificate(MATCHING_CUIL, "221145098",
				LocalDate.now().minusDays(31));

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/applicants/200000/anses-certificate")
						.file(pdf(pdf))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.message")
						.value("La certificación está vencida: tiene más de 30 días desde su emisión"));

		assertThat(ansesCertificateRepository.findByApplicantId(200000L)).isEmpty();
	}

	@Test
	void UploadAnsesCertificate_WhenPdfHasNoBarcode() throws Exception {
		byte[] pdf = AnsesCertificatePdfFactory.withoutBarcode(LocalDate.now());

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/applicants/200000/anses-certificate")
						.file(pdf(pdf))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.message").value(
						"No se pudo leer el código de barras del PDF. Verificá que sea la certificación original de ANSES"));

		assertThat(ansesCertificateRepository.findByApplicantId(200000L)).isEmpty();
	}

	@Test
	void UploadAnsesCertificate_WhenApplicantDoesNotExist() throws Exception {
		byte[] pdf = AnsesCertificatePdfFactory.certificate(MATCHING_CUIL, "221145098", LocalDate.now());

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/applicants/999999/anses-certificate")
						.file(pdf(pdf))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound());
	}

	@Test
	void GetAnsesCertificate_Successful() throws Exception {
		ansesCertificateRepository.save(new AnsesCertificate(200000L, MATCHING_CUIL, "221145098",
				LocalDate.now(), "anses/200000/abc.pdf", "application/pdf", "negativa.pdf"));

		mockMvc.perform(get("/v1/applicants/200000/anses-certificate")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cuil").value(MATCHING_CUIL))
				.andExpect(jsonPath("$.fileOriginalName").value("negativa.pdf"))
				.andExpect(jsonPath("$.fileUrl").value("https://r2.example/firmada"))
				.andExpect(jsonPath("$.fileUrlExpiresAt").isNotEmpty());
	}

	@Test
	void GetAnsesCertificate_WhenApplicantHasNoCertificate() throws Exception {
		mockMvc.perform(get("/v1/applicants/200000/anses-certificate")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message")
						.value("El solicitante no tiene una certificación negativa de ANSES cargada"));
	}

	@Test
	void UploadAnsesCertificate_WithoutToken() throws Exception {
		byte[] pdf = AnsesCertificatePdfFactory.certificate(MATCHING_CUIL, "221145098", LocalDate.now());

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/applicants/200000/anses-certificate")
						.file(pdf(pdf)))
				.andExpect(status().isUnauthorized());
	}
}
