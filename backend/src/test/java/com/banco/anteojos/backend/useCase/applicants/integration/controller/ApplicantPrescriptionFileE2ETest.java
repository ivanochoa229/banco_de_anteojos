package com.banco.anteojos.backend.useCase.applicants.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

import com.banco.anteojos.backend.business.applicants.entities.Prescription;
import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.persistence.applicants.PrescriptionPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/applicants/applicants_template.sql")
class ApplicantPrescriptionFileE2ETest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private PrescriptionPostgresSqlRepository prescriptionRepository;

	// Único mock del test: no se le pega a R2 de verdad en integración.
	@MockitoBean
	private R2StorageClient r2StorageClient;

	private Long prescriptionId;

	@BeforeEach
	void createPrescription() {
		prescriptionId = prescriptionRepository
				.save(new Prescription(200000L, new BigDecimal("-1.25"), null, null, null, null, null))
				.getId();
	}

	private String bearerToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Operador Test", "operator.test@bancoanteojos.org", "hash", Role.OPERATOR));
	}

	private MockMultipartFile pdf() {
		return new MockMultipartFile("file", "receta.pdf", "application/pdf", "%PDF-1.4".getBytes());
	}

	@Test
	void UploadPrescriptionFile_Successful() throws Exception {
		mockMvc.perform(multipart(HttpMethod.PUT,
						"/v1/applicants/200000/prescriptions/" + prescriptionId + "/file")
						.file(pdf())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(prescriptionId))
				.andExpect(jsonPath("$.fileOriginalName").value("receta.pdf"));

		verify(r2StorageClient).upload(any(), any(), eq("application/pdf"));

		Prescription saved = prescriptionRepository.findById(prescriptionId).orElseThrow();
		assertThat(saved.getFileKey()).startsWith("prescriptions/200000/").endsWith(".pdf");
		assertThat(saved.getFileContentType()).isEqualTo("application/pdf");
		assertThat(saved.getFileOriginalName()).isEqualTo("receta.pdf");
	}

	@Test
	void UploadPrescriptionFile_WhenContentTypeNotAllowed() throws Exception {
		mockMvc.perform(multipart(HttpMethod.PUT,
						"/v1/applicants/200000/prescriptions/" + prescriptionId + "/file")
						.file(new MockMultipartFile("file", "receta.zip", "application/zip", new byte[] { 1 }))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("El archivo debe ser PDF, JPG o PNG"));

		verify(r2StorageClient, never()).upload(any(), any(), any());
		assertThat(prescriptionRepository.findById(prescriptionId).orElseThrow().hasFile()).isFalse();
	}

	@Test
	void UploadPrescriptionFile_WhenPrescriptionBelongsToAnotherApplicant() throws Exception {
		mockMvc.perform(multipart(HttpMethod.PUT,
						"/v1/applicants/200000/prescriptions/999999/file")
						.file(pdf())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Receta no encontrada"));

		verify(r2StorageClient, never()).upload(any(), any(), any());
	}

	@Test
	void GetPrescriptionFile_Successful() throws Exception {
		when(r2StorageClient.presignedGetUrl(any()))
				.thenReturn(new PresignedUrl("https://r2.example/firmada", LocalDateTime.now().plusMinutes(15)));

		mockMvc.perform(multipart(HttpMethod.PUT,
						"/v1/applicants/200000/prescriptions/" + prescriptionId + "/file")
						.file(pdf())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());

		mockMvc.perform(get("/v1/applicants/200000/prescriptions/" + prescriptionId + "/file")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value("https://r2.example/firmada"))
				.andExpect(jsonPath("$.fileName").value("receta.pdf"))
				.andExpect(jsonPath("$.contentType").value("application/pdf"))
				.andExpect(jsonPath("$.expiresAt").isNotEmpty());
	}

	@Test
	void GetPrescriptionFile_WhenPrescriptionHasNoFile() throws Exception {
		mockMvc.perform(get("/v1/applicants/200000/prescriptions/" + prescriptionId + "/file")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("La receta no tiene un archivo adjunto"));
	}

	@Test
	void GetPrescriptionFile_WithoutToken() throws Exception {
		mockMvc.perform(get("/v1/applicants/200000/prescriptions/" + prescriptionId + "/file"))
				.andExpect(status().isUnauthorized());
	}
}
