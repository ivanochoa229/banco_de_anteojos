package com.banco.anteojos.backend.useCase.appointments.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.applicants.entities.Applicant;
import com.banco.anteojos.backend.business.applicants.entities.Prescription;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentDay;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus;
import com.banco.anteojos.backend.business.assignments.entities.Assignment;
import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.persistence.applicants.ApplicantPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.applicants.PrescriptionPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.appointments.AppointmentDayPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.appointments.AppointmentPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.assignments.AssignmentPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.notifications.NotificationClient;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;
import com.jayway.jsonpath.JsonPath;

/** Turnos sobre el solicitante 200000 de los templates (Juana Pérez, con email cargado). */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql({ "/sql/applicants/applicants_template.sql", "/sql/frames/frames_template.sql" })
class AppointmentE2ETest {

	private static final long APPLICANT_ID = 200000L;
	// Segundos distintos de cero para que LocalDateTime.toString() coincida con el JSON.
	private static final LocalTime START_TIME = LocalTime.of(10, 30, 15);
	// Primera franja del día de atención: la que le toca al primer turno que se pide.
	private static final LocalDateTime SCHEDULED_AT = LocalDate.now().plusDays(7).atTime(START_TIME);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private AppointmentPostgresSqlRepository appointmentRepository;

	@Autowired
	private AppointmentDayPostgresSqlRepository appointmentDayRepository;

	@Autowired
	private ApplicantPostgresSqlRepository applicantRepository;

	@Autowired
	private PrescriptionPostgresSqlRepository prescriptionRepository;

	@Autowired
	private AssignmentPostgresSqlRepository assignmentRepository;

	@MockitoBean
	private NotificationClient notificationClient;

	@MockitoBean
	private R2StorageClient r2StorageClient;

	private Long dayId;
	private Long nextDayId;

	@BeforeEach
	void setUpAppointmentDays() {
		dayId = appointmentDayRepository.save(new AppointmentDay(SCHEDULED_AT.toLocalDate(), START_TIME, 15, 4))
				.getId();
		nextDayId = appointmentDayRepository
				.save(new AppointmentDay(SCHEDULED_AT.toLocalDate().plusDays(3), START_TIME, 15, 4)).getId();
	}

	private String bearerToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Operador Test", "operator.test@bancoanteojos.org", "hash", Role.OPERATOR));
	}

	// El JWT no valida contra la base: alcanza un User en memoria con el applicantId del claim.
	private String applicantBearerToken(long applicantId) {
		return "Bearer " + jwtService.generateToken(
				new User("Beneficiario Test", "beneficiario.test@mail.com", "hash", Role.APPLICANT, applicantId));
	}

	private String creationBody(Long appointmentDayId) {
		return """
				{"appointmentDayId": %d, "notes": "trae la receta original"}
				""".formatted(appointmentDayId);
	}

	private Long createAppointment() throws Exception {
		String response = mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(dayId))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(response, "$.id")).longValue();
	}

	private void confirmAppointment(Long appointmentId) throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "comprobante.pdf", "application/pdf",
				receiptPdf());
		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/me/appointments/" + appointmentId + "/receipt")
						.file(file)
						.header(HttpHeaders.AUTHORIZATION, applicantBearerToken(APPLICANT_ID)))
				.andExpect(status().isOk());
	}

	private void approveAppointment(Long appointmentId) throws Exception {
		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/approval")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());
	}

	// El administrativo revisa el comprobante y no encuentra problemas: recién ahí queda agendado.
	private Long createConfirmedAppointment() throws Exception {
		Long appointmentId = createAppointment();
		confirmAppointment(appointmentId);
		approveAppointment(appointmentId);
		return appointmentId;
	}

	private byte[] receiptPdf() throws IOException {
		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage();
			document.addPage(page);
			try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
				stream.beginText();
				stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				stream.newLineAtOffset(50, 700);
				stream.showText("Comprobante de pago - Bono contribución");
				stream.endText();
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			document.save(out);
			return out.toByteArray();
		}
	}

	private AppointmentStatus statusInDb(Long appointmentId) {
		return appointmentRepository.findById(appointmentId).orElseThrow().getStatus();
	}

	@Test
	void CreateAppointment_Successful() throws Exception {
		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(dayId))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.applicantId").value(APPLICANT_ID))
				.andExpect(jsonPath("$.scheduledAt").value(SCHEDULED_AT.toString()))
				.andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
				.andExpect(jsonPath("$.assignmentId").isEmpty());

		// Todavía no está confirmado: RF-22 recién sale al subir el comprobante.
		verifyNoInteractions(notificationClient);
	}

	@Test
	void ConfirmAppointment_Successful() throws Exception {
		Long appointmentId = createAppointment();

		confirmAppointment(appointmentId);

		// Todavía no está aprobado: subir el comprobante solo lo deja pendiente de revisión.
		assertThat(statusInDb(appointmentId)).isEqualTo(AppointmentStatus.PENDING_REVIEW);
		verifyNoInteractions(notificationClient);
	}

	@Test
	void Approve_Successful() throws Exception {
		Long appointmentId = createAppointment();
		confirmAppointment(appointmentId);

		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/approval")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SCHEDULED"));

		assertThat(statusInDb(appointmentId)).isEqualTo(AppointmentStatus.SCHEDULED);
		verify(notificationClient).sendAppointmentScheduled(eq("juana.perez@mail.com"), eq("Juana"),
				eq(SCHEDULED_AT));
	}

	@Test
	void Cancel_WhenPendingReview() throws Exception {
		Long appointmentId = createAppointment();
		confirmAppointment(appointmentId);

		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/cancellation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"reason\": \"el comprobante no corresponde\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"));

		assertThat(statusInDb(appointmentId)).isEqualTo(AppointmentStatus.CANCELLED);
	}

	@Test
	void ConfirmAppointment_WhenNotOwner() throws Exception {
		Long appointmentId = createAppointment();
		MockMultipartFile file = new MockMultipartFile("file", "comprobante.pdf", "application/pdf",
				new byte[] { 1, 2, 3 });

		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/me/appointments/" + appointmentId + "/receipt")
						.file(file)
						.header(HttpHeaders.AUTHORIZATION, applicantBearerToken(999999L)))
				.andExpect(status().isNotFound());

		assertThat(statusInDb(appointmentId)).isEqualTo(AppointmentStatus.PENDING_PAYMENT);
	}

	@Test
	void GetReceipt_AfterConfirmation() throws Exception {
		Long appointmentId = createConfirmedAppointment();
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
		when(r2StorageClient.presignedGetUrl(anyString()))
				.thenReturn(new PresignedUrl("https://r2.example/comprobante.pdf", expiresAt));

		mockMvc.perform(get("/v1/appointments/" + appointmentId + "/receipt")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value("https://r2.example/comprobante.pdf"))
				.andExpect(jsonPath("$.fileName").value("comprobante.pdf"));
	}

	@Test
	void CreateAppointment_WhenDayHasPassed() throws Exception {
		Long pastDayId = appointmentDayRepository
				.save(new AppointmentDay(LocalDate.now().minusDays(1), START_TIME, 15, 4)).getId();

		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(pastDayId))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Ese día de atención ya pasó"));

		assertThat(appointmentRepository.count()).isZero();
	}

	@Test
	void CreateAppointment_WithoutAppointmentDay() throws Exception {
		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"notes\": \"sin día\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isBadRequest());

		assertThat(appointmentRepository.count()).isZero();
	}

	@Test
	void CreateAppointment_AssignsConsecutiveSlots() throws Exception {
		createAppointment();

		// El segundo turno del mismo día cae en la franja siguiente, 15 minutos después.
		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(dayId))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.appointmentDayId").value(dayId))
				.andExpect(jsonPath("$.scheduledAt").value(SCHEDULED_AT.plusMinutes(15).toString()));
	}

	@Test
	void CreateAppointment_WhenApplicantNotFound() throws Exception {
		mockMvc.perform(post("/v1/applicants/999999/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(dayId))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound());
	}

	@Test
	void CreateAppointment_AsPickupOfAnAssignment() throws Exception {
		Long prescriptionId = prescriptionRepository
				.save(new Prescription(APPLICANT_ID, new BigDecimal("-1.25"), null, null, null, null, null))
				.getId();
		Long assignmentId = assignmentRepository
				.save(new Assignment(APPLICANT_ID, 400000L, prescriptionId, null)).getId();

		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"appointmentDayId": %d, "assignmentId": %d}
								""".formatted(dayId, assignmentId))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.assignmentId").value(assignmentId));
	}

	@Test
	void CreateAppointment_WhenAssignmentBelongsToAnotherApplicant() throws Exception {
		Long otherApplicantId = applicantRepository.save(new Applicant("Otro", "Beneficiario",
				"20999888", LocalDate.of(1990, 1, 1), null, null)).getId();
		Long prescriptionId = prescriptionRepository
				.save(new Prescription(otherApplicantId, new BigDecimal("-2.00"), null, null, null, null, null))
				.getId();
		Long foreignAssignmentId = assignmentRepository
				.save(new Assignment(otherApplicantId, 400001L, prescriptionId, null)).getId();

		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"appointmentDayId": %d, "assignmentId": %d}
								""".formatted(dayId, foreignAssignmentId))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Asignación no encontrada"));

		// La pertenencia se valida antes de persistir: no queda ningún turno.
		assertThat(appointmentRepository.count()).isZero();
		verifyNoInteractions(notificationClient);
	}

	@Test
	void Reschedule_Successful() throws Exception {
		Long appointmentId = createConfirmedAppointment();
		LocalDateTime newDate = SCHEDULED_AT.plusDays(3);

		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/schedule")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"appointmentDayId\": " + nextDayId + "}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.appointmentDayId").value(nextDayId))
				.andExpect(jsonPath("$.scheduledAt").value(newDate.toString()))
				.andExpect(jsonPath("$.status").value("SCHEDULED"));

		verify(notificationClient).sendAppointmentRescheduled(eq("juana.perez@mail.com"), eq("Juana"),
				eq(newDate));
	}

	@Test
	void Cancel_Successful() throws Exception {
		Long appointmentId = createConfirmedAppointment();

		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/cancellation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"reason\": \"viaja esa semana\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"))
				.andExpect(jsonPath("$.cancellationReason").value("viaja esa semana"));

		assertThat(statusInDb(appointmentId)).isEqualTo(AppointmentStatus.CANCELLED);
		verify(notificationClient).sendAppointmentCancelled(eq("juana.perez@mail.com"), eq("Juana"),
				any(LocalDateTime.class));
	}

	@Test
	void Reschedule_WhenCancelled() throws Exception {
		Long appointmentId = createConfirmedAppointment();
		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/cancellation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"reason\": null}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());

		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/schedule")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"appointmentDayId\": " + nextDayId + "}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("El turno está cancelado"));
	}

	@Test
	void RegisterAttendance_WhenMissed() throws Exception {
		Long appointmentId = createConfirmedAppointment();

		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/attendance")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"attended\": false}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("MISSED"));

		assertThat(statusInDb(appointmentId)).isEqualTo(AppointmentStatus.MISSED);
	}

	@Test
	void ListAppointments_ByDate() throws Exception {
		createAppointment();
		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(nextDayId))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated());

		// La agenda del día solo trae el turno de esa fecha.
		mockMvc.perform(get("/v1/appointments?date=" + SCHEDULED_AT.toLocalDate())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].scheduledAt").value(SCHEDULED_AT.toString()));

		mockMvc.perform(get("/v1/appointments")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void ListAppointmentsByApplicant_Successful() throws Exception {
		createAppointment();

		mockMvc.perform(get("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].applicantId").value(APPLICANT_ID));
	}

	@Test
	void CreateAppointment_WithoutToken() throws Exception {
		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(dayId)))
				.andExpect(status().isUnauthorized());
	}
}
