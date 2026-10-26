package com.banco.anteojos.backend.useCase.assignments.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.frames.entities.Frame;
import com.banco.anteojos.backend.business.frames.entities.FrameMaterial;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;
import com.banco.anteojos.backend.business.frames.entities.FrameType;
import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.orchestrator.frameReservations.FrameReservationUseCaseOrchestrator;
import com.banco.anteojos.backend.persistence.applicants.ApplicantPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.frames.FramePostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;
import com.banco.anteojos.backend.useCase.applicants.AnsesCertificatePdfFactory;
import com.jayway.jsonpath.JsonPath;

import jakarta.persistence.EntityManager;

// El beneficiario reserva desde el probador el marco que le gustó, y el operador se lo asigna.
// Lo que importa: el marco sale del inventario mientras está reservado, nadie más se lo lleva, y
// vuelve solo si la reserva se cancela, vence o el operador asigna otro.
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/frames/frames_template.sql")
class FrameReservationE2ETest {

	private static final long AVAILABLE_FRAME_ID = 400000L;
	private static final long DELIVERED_FRAME_ID = 400001L;
	private static final String DNI_A = "30444555";
	private static final String DNI_B = "30555666";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private ApplicantPostgresSqlRepository applicantRepository;

	@Autowired
	private FramePostgresSqlRepository frameRepository;

	@Autowired
	private FrameReservationUseCaseOrchestrator frameReservationOrchestrator;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EntityManager entityManager;

	// Único mock del test: no se le pega a R2 de verdad en integración.
	@MockitoBean
	private R2StorageClient r2StorageClient;

	@BeforeEach
	void stubPresignedUrl() {
		when(r2StorageClient.presignedGetUrl(any()))
				.thenReturn(new PresignedUrl("https://r2.example/firmada", LocalDateTime.now().plusMinutes(15)));
	}

	private String operatorToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Operador Test", "operator.test@bancoanteojos.org", "hash", Role.OPERATOR));
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

	/** Un beneficiario que cumple los requisitos: negativa de ANSES vigente y receta cargada. */
	private String eligibleApplicant(String dni, String email) throws Exception {
		String token = registerApplicant(dni, email);
		byte[] certificate = AnsesCertificatePdfFactory.certificate(AnsesCertificatePdfFactory.cuilFor(dni),
				"221145098", LocalDate.now());
		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/me/anses-certificate")
						.file(new MockMultipartFile("file", "negativa.pdf", "application/pdf", certificate))
						.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isOk());
		uploadPrescription(token);
		return token;
	}

	private Long uploadPrescription(String token) throws Exception {
		String body = mockMvc.perform(multipart("/v1/me/prescriptions")
						.file(new MockMultipartFile("file", "receta.pdf", "application/pdf", "%PDF-1.4".getBytes()))
						.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(body, "$.id")).longValue();
	}

	private void reserve(String token, long frameId) throws Exception {
		mockMvc.perform(post("/v1/me/frames/" + frameId + "/reservation").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isCreated());
	}

	private Long applicantId(String dni) {
		return applicantRepository.findByDni(dni).orElseThrow().getId();
	}

	private FrameStatus frameStatus(long frameId) {
		return frameRepository.findById(frameId).orElseThrow().getStatus();
	}

	private Long prescriptionOf(String dni) {
		return jdbcTemplate.queryForObject("SELECT id FROM prescriptions WHERE applicant_id = ? LIMIT 1", Long.class,
				applicantId(dni));
	}

	private String assignmentBody(long frameId, long prescriptionId) {
		return "{\"frameId\": %d, \"prescriptionId\": %d}".formatted(frameId, prescriptionId);
	}

	@Test
	void ReserveFrame_Successful() throws Exception {
		String token = eligibleApplicant(DNI_A, "a@mail.com");

		mockMvc.perform(post("/v1/me/frames/" + AVAILABLE_FRAME_ID + "/reservation")
						.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.frame.sealCode").value("A-1001"))
				.andExpect(jsonPath("$.frame.status").value("RESERVED"))
				.andExpect(jsonPath("$.reservation.expiresAt").isNotEmpty());

		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.RESERVED);

		mockMvc.perform(get("/v1/me/reservation").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.frame.id").value(AVAILABLE_FRAME_ID));

		// Ya no se ofrece en el probador de nadie.
		mockMvc.perform(get("/v1/frames").param("status", "AVAILABLE").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(0));
	}

	@Test
	void ReserveFrame_WithoutAnsesCertificate() throws Exception {
		String token = registerApplicant(DNI_A, "a@mail.com");
		uploadPrescription(token);

		mockMvc.perform(post("/v1/me/frames/" + AVAILABLE_FRAME_ID + "/reservation")
						.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(
						"Para reservar un marco necesitás tu certificación negativa de ANSES vigente. "
								+ "Subila desde Mi Perfil"));

		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.AVAILABLE);
	}

	@Test
	void ReserveFrame_WhenFrameIsNotAvailable() throws Exception {
		String token = eligibleApplicant(DNI_A, "a@mail.com");

		mockMvc.perform(post("/v1/me/frames/" + DELIVERED_FRAME_ID + "/reservation")
						.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("El marco ya no está disponible"));

		// El marco se valida antes de registrar la reserva: no queda una reserva colgada.
		mockMvc.perform(get("/v1/me/reservation").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isNotFound());
	}

	@Test
	void ReserveFrame_WhenApplicantAlreadyHasOne() throws Exception {
		String token = eligibleApplicant(DNI_A, "a@mail.com");
		reserve(token, AVAILABLE_FRAME_ID);
		Frame other = frameRepository.save(new Frame(300000L, "A-2000", FrameType.FULL_RIM, FrameMaterial.METAL,
				50, 18, 140));

		mockMvc.perform(post("/v1/me/frames/" + other.getId() + "/reservation")
						.header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message")
						.value("Ya tenés un marco reservado. Cancelá esa reserva para elegir otro"));

		assertThat(frameStatus(other.getId())).isEqualTo(FrameStatus.AVAILABLE);
	}

	@Test
	void CancelMyReservation_ReturnsFrameToInventory() throws Exception {
		String token = eligibleApplicant(DNI_A, "a@mail.com");
		reserve(token, AVAILABLE_FRAME_ID);

		mockMvc.perform(delete("/v1/me/reservation").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isNoContent());

		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.AVAILABLE);
		mockMvc.perform(get("/v1/me/reservation").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isNotFound());
	}

	@Test
	void AssignReservedFrame_FulfillsTheReservation() throws Exception {
		String token = eligibleApplicant(DNI_A, "a@mail.com");
		reserve(token, AVAILABLE_FRAME_ID);
		Long applicantId = applicantId(DNI_A);

		// El operador ve qué eligió antes de asignar.
		mockMvc.perform(get("/v1/applicants/" + applicantId + "/reservation")
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.frame.sealCode").value("A-1001"));

		mockMvc.perform(post("/v1/applicants/" + applicantId + "/assignments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(assignmentBody(AVAILABLE_FRAME_ID, prescriptionOf(DNI_A)))
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isCreated());

		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.ASSIGNED);
		// El test entero es una transacción: sin flush, la consulta cruda no ve la reserva concretada.
		entityManager.flush();
		assertThat(jdbcTemplate.queryForObject(
				"SELECT fulfilled_at IS NOT NULL FROM frame_reservations WHERE applicant_id = ?", Boolean.class,
				applicantId)).isTrue();
		mockMvc.perform(get("/v1/me/reservation").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isNotFound());
	}

	@Test
	void AssignAnotherFrame_ReleasesTheReservedOne() throws Exception {
		String token = eligibleApplicant(DNI_A, "a@mail.com");
		reserve(token, AVAILABLE_FRAME_ID);
		Frame other = frameRepository.save(new Frame(300000L, "A-2000", FrameType.FULL_RIM, FrameMaterial.METAL,
				50, 18, 140));

		mockMvc.perform(post("/v1/applicants/" + applicantId(DNI_A) + "/assignments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(assignmentBody(other.getId(), prescriptionOf(DNI_A)))
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isCreated());

		assertThat(frameStatus(other.getId())).isEqualTo(FrameStatus.ASSIGNED);
		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.AVAILABLE);
	}

	@Test
	void AssignFrameReservedByAnotherApplicant() throws Exception {
		reserve(eligibleApplicant(DNI_A, "a@mail.com"), AVAILABLE_FRAME_ID);
		eligibleApplicant(DNI_B, "b@mail.com");

		mockMvc.perform(post("/v1/applicants/" + applicantId(DNI_B) + "/assignments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(assignmentBody(AVAILABLE_FRAME_ID, prescriptionOf(DNI_B)))
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("El marco está reservado por otro beneficiario"));

		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.RESERVED);
	}

	@Test
	void OperatorCancelsReservation() throws Exception {
		reserve(eligibleApplicant(DNI_A, "a@mail.com"), AVAILABLE_FRAME_ID);

		mockMvc.perform(delete("/v1/applicants/" + applicantId(DNI_A) + "/reservation")
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isNoContent());

		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.AVAILABLE);
	}

	@Test
	void ExpireOverdueReservations_ReturnsFramesToInventory() throws Exception {
		String token = eligibleApplicant(DNI_A, "a@mail.com");
		reserve(token, AVAILABLE_FRAME_ID);
		jdbcTemplate.update("UPDATE frame_reservations SET expires_at = now() - interval '1 minute'");

		frameReservationOrchestrator.expireOverdueReservations();

		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.AVAILABLE);
		mockMvc.perform(get("/v1/me/reservation").header(HttpHeaders.AUTHORIZATION, token))
				.andExpect(status().isNotFound());
	}

	@Test
	void ReserveFrame_AsOperatorIsForbidden() throws Exception {
		mockMvc.perform(post("/v1/me/frames/" + AVAILABLE_FRAME_ID + "/reservation")
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isForbidden());
	}
}
