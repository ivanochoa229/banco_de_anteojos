package com.banco.anteojos.backend.useCase.frames.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

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

import com.banco.anteojos.backend.business.frames.entities.Frame;
import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.persistence.frames.FramePostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/frames/frames_template.sql")
class FrameImageE2ETest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private FramePostgresSqlRepository frameRepository;

	// Único mock del test: no se le pega a R2 de verdad en integración.
	@MockitoBean
	private R2StorageClient r2StorageClient;

	private String bearerToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Operador Test", "operator.test@bancoanteojos.org", "hash", Role.OPERATOR));
	}

	private MockMultipartFile png() {
		return new MockMultipartFile("file", "marco.png", "image/png", new byte[] { (byte) 0x89, 'P', 'N', 'G' });
	}

	@Test
	void UploadFrameImage_Successful() throws Exception {
		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/frames/400000/image")
						.file(png())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(400000))
				.andExpect(jsonPath("$.imageOriginalName").value("marco.png"));

		verify(r2StorageClient).upload(any(), any(), eq("image/png"));

		Frame saved = frameRepository.findById(400000L).orElseThrow();
		assertThat(saved.getImageKey()).startsWith("frames/400000/").endsWith(".png");
		assertThat(saved.getImageContentType()).isEqualTo("image/png");
		assertThat(saved.getImageOriginalName()).isEqualTo("marco.png");
	}

	@Test
	void UploadFrameImage_WhenFileIsJpeg() throws Exception {
		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/frames/400000/image")
						.file(new MockMultipartFile("file", "marco.jpg", "image/jpeg", new byte[] { 1 }))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(containsString("PNG o WEBP")));

		verify(r2StorageClient, never()).upload(any(), any(), any());
		assertThat(frameRepository.findById(400000L).orElseThrow().hasImage()).isFalse();
	}

	@Test
	void UploadFrameImage_WhenFrameDoesNotExist() throws Exception {
		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/frames/999999/image")
						.file(png())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound());

		verify(r2StorageClient, never()).upload(any(), any(), any());
	}

	@Test
	void GetFrameImage_Successful() throws Exception {
		when(r2StorageClient.presignedGetUrl(any()))
				.thenReturn(new PresignedUrl("https://r2.example/firmada", LocalDateTime.now().plusMinutes(15)));

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/frames/400000/image")
						.file(png())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());

		mockMvc.perform(get("/v1/frames/400000/image")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value("https://r2.example/firmada"))
				.andExpect(jsonPath("$.fileName").value("marco.png"))
				.andExpect(jsonPath("$.contentType").value("image/png"));
	}

	@Test
	void GetFrameImage_WhenFrameHasNoImage() throws Exception {
		mockMvc.perform(get("/v1/frames/400000/image")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("El marco no tiene una foto cargada"));
	}

	@Test
	void GetFrameImage_WhenNotAuthenticated() throws Exception {
		mockMvc.perform(get("/v1/frames/400000/image"))
				.andExpect(status().isUnauthorized());
	}
}
