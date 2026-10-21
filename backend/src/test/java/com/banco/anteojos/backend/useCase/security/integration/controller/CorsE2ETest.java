package com.banco.anteojos.backend.useCase.security.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

// En producción el front y la API viven en dominios distintos: sin CORS el navegador bloquea
// todos los requests. El origen permitido sale de cors.allowed-origins (https://front.test en tests).
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
class CorsE2ETest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void Preflight_WhenOriginIsAllowed() throws Exception {
		mockMvc.perform(options("/v1/frames")
						.header("Origin", "https://front.test")
						.header("Access-Control-Request-Method", "GET")
						.header("Access-Control-Request-Headers", "Authorization"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "https://front.test"))
				.andExpect(header().string("Access-Control-Allow-Headers", "Authorization"));
	}

	@Test
	void Preflight_WhenOriginIsNotAllowed() throws Exception {
		mockMvc.perform(options("/v1/frames")
						.header("Origin", "https://otro-sitio.test")
						.header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
	}

	@Test
	void Request_WhenOriginIsAllowedButNotAuthenticated() throws Exception {
		// CORS no reemplaza la autenticación: el origen permitido igual necesita JWT.
		mockMvc.perform(get("/v1/frames").header("Origin", "https://front.test"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("Access-Control-Allow-Origin", "https://front.test"));
	}
}
