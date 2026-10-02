package com.banco.anteojos.backend.useCase.appointments.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

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

import com.banco.anteojos.backend.business.appointments.entities.Appointment;
import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.persistence.appointments.AppointmentDayPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.appointments.AppointmentPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.notifications.NotificationClient;
import com.jayway.jsonpath.JsonPath;

/** Días de atención (RF-20) y su cupo, reservando sobre el solicitante 200000 de los templates. */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/applicants/applicants_template.sql")
class AppointmentDayE2ETest {

	private static final long APPLICANT_ID = 200000L;
	private static final LocalDate DATE = LocalDate.now().plusDays(9);

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private AppointmentDayPostgresSqlRepository appointmentDayRepository;

	@Autowired
	private AppointmentPostgresSqlRepository appointmentRepository;

	@MockitoBean
	private NotificationClient notificationClient;

	private String operatorToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Operador Test", "operator.test@bancoanteojos.org", "hash", Role.OPERATOR));
	}

	private String applicantToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Beneficiario Test", "beneficiario.test@mail.com", "hash", Role.APPLICANT, APPLICANT_ID));
	}

	// Turnos de 15 minutos: el fin se arma para que entren exactamente slotCount.
	private String dayBody(LocalDate date, String startTime, int slotCount) {
		LocalTime endTime = LocalTime.parse(startTime).plusMinutes(15L * slotCount);
		return """
				{"date": "%s", "startTime": "%s", "endTime": "%s", "slotDurationMinutes": 15}
				""".formatted(date, startTime, endTime);
	}

	private Long createDay(int slotCount) throws Exception {
		String response = mockMvc.perform(post("/v1/appointment-days")
						.contentType(MediaType.APPLICATION_JSON)
						.content(dayBody(DATE, "09:00", slotCount))
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(response, "$.id")).longValue();
	}

	private Long bookAsApplicant(Long dayId) throws Exception {
		String response = mockMvc.perform(post("/v1/me/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"appointmentDayId\": " + dayId + "}")
						.header(HttpHeaders.AUTHORIZATION, applicantToken()))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(response, "$.id")).longValue();
	}

	@Test
	void CreateDay_Successful() throws Exception {
		mockMvc.perform(post("/v1/appointment-days")
						.contentType(MediaType.APPLICATION_JSON)
						.content(dayBody(DATE, "09:00", 16))
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.date").value(DATE.toString()))
				.andExpect(jsonPath("$.startTime").value("09:00:00"))
				.andExpect(jsonPath("$.endTime").value("13:00:00"))
				.andExpect(jsonPath("$.slotCount").value(16))
				.andExpect(jsonPath("$.availableCount").value(16));

		assertThat(appointmentDayRepository.count()).isEqualTo(1);
	}

	@Test
	void CreateDay_WhenDateAlreadyExists() throws Exception {
		createDay(4);

		mockMvc.perform(post("/v1/appointment-days")
						.contentType(MediaType.APPLICATION_JSON)
						.content(dayBody(DATE, "14:00", 4))
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isConflict());

		assertThat(appointmentDayRepository.count()).isEqualTo(1);
	}

	@Test
	void CreateDay_WhenDateIsInThePast() throws Exception {
		mockMvc.perform(post("/v1/appointment-days")
						.contentType(MediaType.APPLICATION_JSON)
						.content(dayBody(LocalDate.now().minusDays(1), "09:00", 4))
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isBadRequest());

		assertThat(appointmentDayRepository.count()).isZero();
	}

	@Test
	void CreateDay_AsApplicant() throws Exception {
		mockMvc.perform(post("/v1/appointment-days")
						.contentType(MediaType.APPLICATION_JSON)
						.content(dayBody(DATE, "09:00", 4))
						.header(HttpHeaders.AUTHORIZATION, applicantToken()))
				.andExpect(status().isForbidden());
	}

	@Test
	void BookAppointment_WhenDayIsFull() throws Exception {
		Long dayId = createDay(1);
		bookAsApplicant(dayId);

		mockMvc.perform(post("/v1/me/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"appointmentDayId\": " + dayId + "}")
						.header(HttpHeaders.AUTHORIZATION, applicantToken()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("No quedan turnos libres para ese día"));

		assertThat(appointmentRepository.count()).isEqualTo(1);
		// Lleno, el día deja de ofrecérsele al beneficiario pero el personal lo sigue viendo.
		mockMvc.perform(get("/v1/me/appointment-days").header(HttpHeaders.AUTHORIZATION, applicantToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
		mockMvc.perform(get("/v1/appointment-days").header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].bookedCount").value(1))
				.andExpect(jsonPath("$[0].availableCount").value(0));
	}

	@Test
	void BookAppointment_AfterCancellationFreesTheSlot() throws Exception {
		Long dayId = createDay(1);
		Long appointmentId = bookAsApplicant(dayId);

		// El comprobante y su revisión ya tienen su propio E2E: acá se lo lleva a revisión por la
		// entidad para no depender de R2, y la cancelación sí pasa por el endpoint real.
		Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
		appointment.submitReceiptForReview("appointments/1/comprobante.pdf", "application/pdf", "comprobante.pdf");
		appointmentRepository.saveAndFlush(appointment);
		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/cancellation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"reason\": \"no puede venir\"}")
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isOk());

		mockMvc.perform(get("/v1/me/appointment-days").header(HttpHeaders.AUTHORIZATION, applicantToken()))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].availableCount").value(1));
		mockMvc.perform(post("/v1/me/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"appointmentDayId\": " + dayId + "}")
						.header(HttpHeaders.AUTHORIZATION, applicantToken()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.scheduledAt").value(DATE.atTime(9, 0).toString() + ":00"));
	}

	@Test
	void UpdateDay_WhenShrinkingBelowBookings() throws Exception {
		Long dayId = createDay(4);
		bookAsApplicant(dayId);
		bookAsApplicant(dayId);

		mockMvc.perform(put("/v1/appointment-days/" + dayId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(dayBody(DATE, "09:00", 1))
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("No se puede adelantar el fin: el turno de las 09:15 ya está dado"));

		assertThat(appointmentDayRepository.findById(dayId).orElseThrow().getSlotCount()).isEqualTo(4);
	}

	@Test
	void UpdateDay_IncreasesSlotCount() throws Exception {
		Long dayId = createDay(4);
		bookAsApplicant(dayId);

		mockMvc.perform(put("/v1/appointment-days/" + dayId)
						.contentType(MediaType.APPLICATION_JSON)
						.content(dayBody(DATE, "09:00", 8))
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slotCount").value(8))
				.andExpect(jsonPath("$.availableCount").value(7));

		assertThat(appointmentDayRepository.findById(dayId).orElseThrow().getSlotCount()).isEqualTo(8);
	}

	@Test
	void DeleteDay_WhenHasActiveBookings() throws Exception {
		Long dayId = createDay(4);
		bookAsApplicant(dayId);

		mockMvc.perform(delete("/v1/appointment-days/" + dayId)
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isConflict());

		assertThat(appointmentDayRepository.existsById(dayId)).isTrue();
	}

	@Test
	void DeleteDay_Successful() throws Exception {
		Long dayId = createDay(4);

		mockMvc.perform(delete("/v1/appointment-days/" + dayId)
						.header(HttpHeaders.AUTHORIZATION, operatorToken()))
				.andExpect(status().isNoContent());

		assertThat(appointmentDayRepository.existsById(dayId)).isFalse();
	}
}
