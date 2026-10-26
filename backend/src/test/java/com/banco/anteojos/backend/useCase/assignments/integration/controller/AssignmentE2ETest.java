package com.banco.anteojos.backend.useCase.assignments.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.applicants.entities.AnsesCertificate;
import com.banco.anteojos.backend.business.applicants.entities.Prescription;
import com.banco.anteojos.backend.business.assignments.entities.Assignment;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;
import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.persistence.applicants.AnsesCertificatePostgresSqlRepository;
import com.banco.anteojos.backend.persistence.applicants.PrescriptionPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.assignments.AssignmentPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.frames.FramePostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.notifications.EmailAttachment;
import com.banco.anteojos.backend.thirdPartyServiceComunication.notifications.NotificationClient;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;
import com.jayway.jsonpath.JsonPath;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Circuito completo con datos de los templates: solicitante 200000, donante 300000 y marcos
 * 400000 (disponible) y 400001 (ya entregado).
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql({ "/sql/applicants/applicants_template.sql", "/sql/frames/frames_template.sql" })
class AssignmentE2ETest {

	private static final long APPLICANT_ID = 200000L;
	private static final long AVAILABLE_FRAME_ID = 400000L;
	private static final long DELIVERED_FRAME_ID = 400001L;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private PrescriptionPostgresSqlRepository prescriptionRepository;

	@Autowired
	private AnsesCertificatePostgresSqlRepository ansesCertificateRepository;

	@Autowired
	private FramePostgresSqlRepository frameRepository;

	@Autowired
	private AssignmentPostgresSqlRepository assignmentRepository;

	@MockitoBean
	private R2StorageClient r2StorageClient;

	// Mockeado para verificar el aviso al donante sin depender de un SMTP.
	@MockitoBean
	private NotificationClient notificationClient;

	private Long prescriptionId;

	@BeforeEach
	void createPrescription() {
		prescriptionId = prescriptionRepository
				.save(new Prescription(APPLICANT_ID, new BigDecimal("-1.25"), null, null, null, null, null))
				.getId();
	}

	private String bearerToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Operador Test", "operator.test@bancoanteojos.org", "hash", Role.OPERATOR));
	}

	/** El solicitante del template no tiene negativa cargada: sin esto, toda asignación advierte. */
	private void givenValidAnsesCertificate() {
		ansesCertificateRepository.save(new AnsesCertificate(APPLICANT_ID, "20301234563", "221145098",
				LocalDate.now(), "anses/200000/abc.pdf", "application/pdf", "negativa.pdf"));
	}

	private String creationBody(Long frameId) {
		return creationBody(frameId, prescriptionId);
	}

	private String creationBody(Long frameId, Long prescription) {
		return """
				{"frameId": %d, "prescriptionId": %d, "notes": "retira el jueves"}
				""".formatted(frameId, prescription);
	}

	private Long createAssignment(Long frameId) throws Exception {
		String response = mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/assignments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(frameId))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(response, "$.assignment.id")).longValue();
	}

	private FrameStatus frameStatus(Long frameId) {
		return frameRepository.findById(frameId).orElseThrow().getStatus();
	}

	@Test
	void CreateAssignment_Successful() throws Exception {
		givenValidAnsesCertificate();

		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/assignments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(AVAILABLE_FRAME_ID))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.assignment.applicantId").value(APPLICANT_ID))
				.andExpect(jsonPath("$.assignment.frameId").value(AVAILABLE_FRAME_ID))
				.andExpect(jsonPath("$.assignment.assignedAt").isNotEmpty())
				.andExpect(jsonPath("$.assignment.deliveredAt").isEmpty())
				.andExpect(jsonPath("$.eligibilityWarning").isEmpty());

		// El marco sale del inventario disponible (RF-15).
		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.ASSIGNED);
	}

	@Test
	void CreateAssignment_WhenApplicantHasNoAnsesCertificate() throws Exception {
		// Falta la negativa: se asigna igual y la respuesta lo advierte, la decisión es del operador.
		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/assignments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(AVAILABLE_FRAME_ID))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.eligibilityWarning")
						.value("El solicitante no tiene una certificación negativa de ANSES cargada"));

		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.ASSIGNED);
	}

	@Test
	void CreateAssignment_WhenFrameIsNotAvailable() throws Exception {
		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/assignments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(DELIVERED_FRAME_ID))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("El marco no está disponible en el inventario"));
	}

	@Test
	void CreateAssignment_WhenPrescriptionBelongsToAnotherApplicant() throws Exception {
		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/assignments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(AVAILABLE_FRAME_ID, 999999L))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Receta no encontrada"));

		// La pertenencia se valida antes de tocar nada: el marco sigue disponible.
		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.AVAILABLE);
	}

	@Test
	void FullCircuit_Successful() throws Exception {
		givenValidAnsesCertificate();
		Long assignmentId = createAssignment(AVAILABLE_FRAME_ID);

		mockMvc.perform(put("/v1/assignments/" + assignmentId + "/optician-dispatch")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sentToOpticianAt").isNotEmpty());
		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.AT_OPTICIAN);

		mockMvc.perform(put("/v1/assignments/" + assignmentId + "/optician-return")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.returnedAt").isNotEmpty());
		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.READY);

		mockMvc.perform(put("/v1/assignments/" + assignmentId + "/delivery")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.deliveredAt").isNotEmpty())
				.andExpect(jsonPath("$.deliveryPhotoOriginalName").isEmpty());
		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.DELIVERED);
		// Sin foto, el donante recibe el aviso igual.
		verify(notificationClient).sendFrameDelivered(eq("contacto@rotary.org"), eq("Rotary Club Tucumán"),
				eq("A-1001"), isNull());
	}

	/** Lleva la asignación hasta "lista para entregar" (volvió de la óptica). */
	private Long readyAssignment() throws Exception {
		Long assignmentId = createAssignment(AVAILABLE_FRAME_ID);
		mockMvc.perform(put("/v1/assignments/" + assignmentId + "/optician-dispatch")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());
		mockMvc.perform(put("/v1/assignments/" + assignmentId + "/optician-return")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());
		return assignmentId;
	}

	private MockMultipartFile deliveryPhoto(String contentType) {
		return new MockMultipartFile("photo", "entrega.jpg", contentType, new byte[] { 1, 2, 3 });
	}

	@Test
	void Deliver_WithPhotoAndConsent() throws Exception {
		Long assignmentId = readyAssignment();

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/assignments/" + assignmentId + "/delivery")
						.file(deliveryPhoto("image/jpeg"))
						.param("photoConsent", "true")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.deliveredAt").isNotEmpty())
				.andExpect(jsonPath("$.deliveryPhotoOriginalName").value("entrega.jpg"))
				.andExpect(jsonPath("$.deliveryPhotoConsent").value(true));

		Assignment saved = assignmentRepository.findById(assignmentId).orElseThrow();
		assertThat(saved.getDeliveryPhotoKey()).startsWith("deliveries/" + assignmentId + "/").endsWith(".jpg");
		assertThat(saved.isDeliveryPhotoConsent()).isTrue();
		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.DELIVERED);
		verify(r2StorageClient).upload(eq(saved.getDeliveryPhotoKey()), any(), eq("image/jpeg"));

		ArgumentCaptor<EmailAttachment> photo = ArgumentCaptor.forClass(EmailAttachment.class);
		verify(notificationClient).sendFrameDelivered(eq("contacto@rotary.org"), eq("Rotary Club Tucumán"),
				eq("A-1001"), photo.capture());
		assertThat(photo.getValue().fileName()).isEqualTo("entrega.jpg");
		assertThat(photo.getValue().content()).containsExactly(1, 2, 3);
	}

	@Test
	void Deliver_WithPhotoWithoutConsent() throws Exception {
		Long assignmentId = readyAssignment();

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/assignments/" + assignmentId + "/delivery")
						.file(deliveryPhoto("image/jpeg"))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.deliveryPhotoOriginalName").value("entrega.jpg"))
				.andExpect(jsonPath("$.deliveryPhotoConsent").value(false));

		// La foto queda guardada, pero sin el permiso del beneficiario no se le manda al donante.
		assertThat(assignmentRepository.findById(assignmentId).orElseThrow().getDeliveryPhotoKey()).isNotNull();
		verify(notificationClient).sendFrameDelivered(eq("contacto@rotary.org"), eq("Rotary Club Tucumán"),
				eq("A-1001"), isNull());
	}

	@Test
	void Deliver_WhenPhotoIsNotAnImage() throws Exception {
		Long assignmentId = readyAssignment();

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/assignments/" + assignmentId + "/delivery")
						.file(deliveryPhoto("application/pdf"))
						.param("photoConsent", "true")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("La foto de la entrega debe ser JPG, PNG o WEBP"));

		// No se registra nada: ni la entrega, ni el marco, ni el aviso.
		assertThat(assignmentRepository.findById(assignmentId).orElseThrow().getDeliveredAt()).isNull();
		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.READY);
		verify(notificationClient, never()).sendFrameDelivered(any(), any(), any(), any());
	}

	@Test
	void GetDeliveryPhoto_Successful() throws Exception {
		when(r2StorageClient.presignedGetUrl(any()))
				.thenReturn(new PresignedUrl("https://r2.example/firmada", LocalDateTime.now().plusMinutes(15)));
		Long assignmentId = readyAssignment();
		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/assignments/" + assignmentId + "/delivery")
						.file(deliveryPhoto("image/jpeg"))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());

		mockMvc.perform(get("/v1/assignments/" + assignmentId + "/delivery-photo")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value("https://r2.example/firmada"))
				.andExpect(jsonPath("$.fileName").value("entrega.jpg"));
	}

	@Test
	void GetDeliveryPhoto_WhenThereIsNone() throws Exception {
		Long assignmentId = createAssignment(AVAILABLE_FRAME_ID);

		mockMvc.perform(get("/v1/assignments/" + assignmentId + "/delivery-photo")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("La entrega no tiene una foto cargada"));
	}

	@Test
	void Deliver_WhenFrameNeverWentToOptician() throws Exception {
		Long assignmentId = createAssignment(AVAILABLE_FRAME_ID);

		mockMvc.perform(put("/v1/assignments/" + assignmentId + "/delivery")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(
						"No se puede entregar: el marco todavía no volvió de la óptica con los cristales"));

		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.ASSIGNED);
	}

	@Test
	void Cancel_ReturnsFrameToInventory() throws Exception {
		Long assignmentId = createAssignment(AVAILABLE_FRAME_ID);

		mockMvc.perform(put("/v1/assignments/" + assignmentId + "/cancellation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"reason\":\"el beneficiario no se presentó\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cancelledAt").isNotEmpty())
				.andExpect(jsonPath("$.cancellationReason").value("el beneficiario no se presentó"));

		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.AVAILABLE);
	}

	@Test
	void CreateAssignment_AfterCancellingThePreviousOne() throws Exception {
		Long cancelled = createAssignment(AVAILABLE_FRAME_ID);
		mockMvc.perform(put("/v1/assignments/" + cancelled + "/cancellation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"reason\":\"cambio de marco\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());
		// En producción cada request commitea por su cuenta; acá el test los envuelve a todos en
		// una transacción, así que hay que forzar el límite para que el UPDATE del cancelled_at
		// llegue a la DB antes del INSERT siguiente (Hibernate ordena los INSERT primero).
		entityManager.flush();

		// El marco liberado se puede volver a asignar: el índice único solo cuenta las no canceladas.
		createAssignment(AVAILABLE_FRAME_ID);

		assertThat(frameStatus(AVAILABLE_FRAME_ID)).isEqualTo(FrameStatus.ASSIGNED);
	}

	@Test
	void GetAssignment_ReturnsEndToEndTraceability() throws Exception {
		Long assignmentId = createAssignment(AVAILABLE_FRAME_ID);

		mockMvc.perform(get("/v1/assignments/" + assignmentId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assignment.id").value(assignmentId))
				.andExpect(jsonPath("$.applicant.dni").value("30123456"))
				.andExpect(jsonPath("$.frame.sealCode").value("A-1001"))
				// Desde la donación hasta la entrega: el donante que aportó el marco (RF-16).
				.andExpect(jsonPath("$.donor.name").value("Rotary Club Tucumán"));
	}

	@Test
	void ListAssignmentsByApplicant_Successful() throws Exception {
		createAssignment(AVAILABLE_FRAME_ID);

		mockMvc.perform(get("/v1/applicants/" + APPLICANT_ID + "/assignments")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].frameId").value(AVAILABLE_FRAME_ID));
	}

	@Test
	void ListAssignments_OnlyLive() throws Exception {
		Long cancelled = createAssignment(AVAILABLE_FRAME_ID);
		mockMvc.perform(put("/v1/assignments/" + cancelled + "/cancellation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"reason\":\"prueba\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());

		mockMvc.perform(get("/v1/assignments?live=true")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(0));

		mockMvc.perform(get("/v1/assignments")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1));
	}

	@Test
	void GetEligibility_WhenCertificateIsInForce() throws Exception {
		givenValidAnsesCertificate();

		mockMvc.perform(get("/v1/applicants/" + APPLICANT_ID + "/eligibility")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.eligible").value(true))
				.andExpect(jsonPath("$.warning").isEmpty());
	}

	@Test
	void CreateAssignment_WithoutToken() throws Exception {
		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/assignments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(AVAILABLE_FRAME_ID)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void ListAssignments_IncludesApplicantNameAndSealCode() throws Exception {
		createAssignment(AVAILABLE_FRAME_ID);

		mockMvc.perform(get("/v1/assignments")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].assignment.frameId").value(AVAILABLE_FRAME_ID))
				.andExpect(jsonPath("$.content[0].applicantName").value("Pérez, Juana"))
				.andExpect(jsonPath("$.content[0].frameSealCode").value("A-1001"));
	}
}
