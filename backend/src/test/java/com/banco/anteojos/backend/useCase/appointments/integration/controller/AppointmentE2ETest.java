package com.banco.anteojos.backend.useCase.appointments.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.applicants.entities.Applicant;
import com.banco.anteojos.backend.business.applicants.entities.Prescription;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus;
import com.banco.anteojos.backend.business.assignments.entities.Assignment;
import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.persistence.applicants.ApplicantPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.applicants.PrescriptionPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.appointments.AppointmentPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.assignments.AssignmentPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.notifications.NotificationClient;
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
	private static final LocalDateTime SCHEDULED_AT =
			LocalDateTime.now().plusDays(7).withHour(10).withMinute(30).withSecond(15).withNano(0);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private AppointmentPostgresSqlRepository appointmentRepository;

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

	private String bearerToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Operador Test", "operator.test@bancoanteojos.org", "hash", Role.OPERATOR));
	}

	private String creationBody(LocalDateTime scheduledAt) {
		return """
				{"scheduledAt": "%s", "notes": "trae la receta original"}
				""".formatted(scheduledAt);
	}

	private Long createAppointment() throws Exception {
		String response = mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(SCHEDULED_AT))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(response, "$.id")).longValue();
	}

	private AppointmentStatus statusInDb(Long appointmentId) {
		return appointmentRepository.findById(appointmentId).orElseThrow().getStatus();
	}

	@Test
	void CreateAppointment_Successful() throws Exception {
		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(SCHEDULED_AT))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.applicantId").value(APPLICANT_ID))
				.andExpect(jsonPath("$.scheduledAt").value(SCHEDULED_AT.toString()))
				.andExpect(jsonPath("$.status").value("SCHEDULED"))
				.andExpect(jsonPath("$.assignmentId").isEmpty());

		// La confirmación de RF-22 sale con el email del template.
		verify(notificationClient).sendAppointmentScheduled(eq("juana.perez@mail.com"), eq("Juana"),
				eq(SCHEDULED_AT));
	}

	@Test
	void CreateAppointment_WhenDateIsInThePast() throws Exception {
		mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(LocalDateTime.now().minusDays(1)))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isBadRequest());

		assertThat(appointmentRepository.count()).isZero();
	}

	@Test
	void CreateAppointment_WhenApplicantNotFound() throws Exception {
		mockMvc.perform(post("/v1/applicants/999999/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody(SCHEDULED_AT))
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
								{"scheduledAt": "%s", "assignmentId": %d}
								""".formatted(SCHEDULED_AT, assignmentId))
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
								{"scheduledAt": "%s", "assignmentId": %d}
								""".formatted(SCHEDULED_AT, foreignAssignmentId))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Asignación no encontrada"));

		// La pertenencia se valida antes de persistir: no queda ningún turno.
		assertThat(appointmentRepository.count()).isZero();
		verifyNoInteractions(notificationClient);
	}

	@Test
	void Reschedule_Successful() throws Exception {
		Long appointmentId = createAppointment();
		LocalDateTime newDate = SCHEDULED_AT.plusDays(3);

		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/schedule")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"scheduledAt\": \"" + newDate + "\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.scheduledAt").value(newDate.toString()))
				.andExpect(jsonPath("$.status").value("SCHEDULED"));

		verify(notificationClient).sendAppointmentRescheduled(eq("juana.perez@mail.com"), eq("Juana"),
				eq(newDate));
	}

	@Test
	void Cancel_Successful() throws Exception {
		Long appointmentId = createAppointment();

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
		Long appointmentId = createAppointment();
		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/cancellation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"reason\": null}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());

		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/schedule")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"scheduledAt\": \"" + SCHEDULED_AT.plusDays(1) + "\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("El turno está cancelado"));
	}

	@Test
	void RegisterAttendance_WhenMissed() throws Exception {
		Long appointmentId = createAppointment();

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
						.content(creationBody(SCHEDULED_AT.plusDays(1)))
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
						.content(creationBody(SCHEDULED_AT)))
				.andExpect(status().isUnauthorized());
	}
}
