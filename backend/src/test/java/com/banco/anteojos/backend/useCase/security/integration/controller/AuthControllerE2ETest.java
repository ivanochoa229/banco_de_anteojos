package com.banco.anteojos.backend.useCase.security.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.security.JwtService;
import com.jayway.jsonpath.JsonPath;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/security/users_template.sql")
class AuthControllerE2ETest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Test
	void Login_Successful() throws Exception {
		String body = mockMvc.perform(post("/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "operator.test@bancoanteojos.org", "password": "password123"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andReturn().getResponse().getContentAsString();

		String token = JsonPath.read(body, "$.token");
		assertThat(jwtService.validate(token)).hasValueSatisfying(tokenData -> {
			assertThat(tokenData.email()).isEqualTo("operator.test@bancoanteojos.org");
			assertThat(tokenData.role()).isEqualTo("OPERATOR");
		});
	}

	@Test
	void Login_WhenPasswordIsWrong() throws Exception {
		mockMvc.perform(post("/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "operator.test@bancoanteojos.org", "password": "wrong-password"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.error").value("Unauthorized"))
				.andExpect(jsonPath("$.message").value("Credenciales inválidas"))
				.andExpect(jsonPath("$.path").value("/v1/auth/login"))
				.andExpect(jsonPath("$.timestamp").isNotEmpty());
	}
}
