package com.banco.anteojos.backend.useCase.indicators.integration.controller;

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
import java.time.format.DateTimeFormatter;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.applicants.entities.Prescription;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentDay;
import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.business.shipments.entities.Shipment;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentStatus;
import com.banco.anteojos.backend.persistence.applicants.PrescriptionPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.appointments.AppointmentDayPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.shipments.ShipmentPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.notifications.NotificationClient;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;
import com.banco.anteojos.backend.thirdPartyServiceComunication.tracking.TrackingClient;
import com.jayway.jsonpath.JsonPath;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Panel de impacto (RF-26/27) con el solicitante 200000 y los marcos 400000/400001 de los
 * templates. Arma el circuito completo por la API (asignaciones, turnos, envío) y solo recurre
 * a SQL directo para correr {@code delivered_at} de las asignaciones a dos meses distintos: la
 * entidad no expone un setter de fecha a propósito, y acá hace falta un dato histórico. El
 * {@code flush()+clear()} después de cada UPDATE crudo evita que el auto-flush de Hibernate,
 * disparado más tarde por cualquier query, pise el dato con el estado en memoria que dejó el
 * flujo por la API.
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql({ "/sql/applicants/applicants_template.sql", "/sql/frames/frames_template.sql" })
class IndicatorsE2ETest {

	private static final long APPLICANT_ID = 200000L;
	private static final long FRAME_1 = 400000L;
	private static final long FRAME_2 = 400001L;
	private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private PrescriptionPostgresSqlRepository prescriptionRepository;

	@Autowired
	private ShipmentPostgresSqlRepository shipmentRepository;

	@Autowired
	private AppointmentDayPostgresSqlRepository appointmentDayRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@PersistenceContext
	private EntityManager entityManager;

	@MockitoBean
	private TrackingClient trackingClient;

	@MockitoBean
	private NotificationClient notificationClient;

	@MockitoBean
	private R2StorageClient r2StorageClient;

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

	private String applicantBearerToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Beneficiario Test", "beneficiario.test@mail.com", "hash", Role.APPLICANT, APPLICANT_ID));
	}

	private Long createAndDeliverAssignment(Long frameId, LocalDateTime deliveredAt) throws Exception {
		// El marco 400001 del template llega DELIVERED; el circuito real exige que esté
		// AVAILABLE antes de asignarlo.
		jdbcTemplate.update("UPDATE frames SET status = 'AVAILABLE' WHERE id = ?", frameId);
		entityManager.flush();
		entityManager.clear();

		String response = mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/assignments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"frameId": %d, "prescriptionId": %d}
								""".formatted(frameId, prescriptionId))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		Long assignmentId = ((Number) JsonPath.read(response, "$.assignment.id")).longValue();

		mockMvc.perform(put("/v1/assignments/" + assignmentId + "/optician-dispatch")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())).andExpect(status().isOk());
		mockMvc.perform(put("/v1/assignments/" + assignmentId + "/optician-return")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())).andExpect(status().isOk());
		mockMvc.perform(put("/v1/assignments/" + assignmentId + "/delivery")
				.header(HttpHeaders.AUTHORIZATION, bearerToken())).andExpect(status().isOk());

		// Vuelca a la DB lo que dejó pendiente el flujo por la API antes de correr la fecha
		// a mano: si no, el auto-flush de una query posterior pisaría este UPDATE.
		entityManager.flush();
		jdbcTemplate.update("UPDATE assignments SET delivered_at = ? WHERE id = ?", deliveredAt, assignmentId);
		entityManager.clear();
		return assignmentId;
	}

	// Un día de atención por turno, arrancando a esa hora: el turno cae en su primera franja.
	// Franja de 1 minuto para que, si el test corre cerca de medianoche, no pase al día siguiente.
	private Long createAppointment(LocalDateTime scheduledAt) throws Exception {
		Long dayId = appointmentDayRepository.save(new AppointmentDay(scheduledAt.toLocalDate(),
				scheduledAt.toLocalTime(), 1, 1)).getId();
		String response = mockMvc.perform(post("/v1/applicants/" + APPLICANT_ID + "/appointments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"appointmentDayId": %d}
								""".formatted(dayId))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(response, "$.id")).longValue();
	}

	// El comprobante queda pendiente de revisión al subirlo: hace falta aprobarlo para que el
	// turno llegue a SCHEDULED y pueda registrarse la asistencia.
	private void confirmAppointment(Long appointmentId) throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "comprobante.pdf", "application/pdf",
				receiptPdf());
		mockMvc.perform(multipart(HttpMethod.PUT, "/v1/me/appointments/" + appointmentId + "/receipt")
						.file(file)
						.header(HttpHeaders.AUTHORIZATION, applicantBearerToken()))
				.andExpect(status().isOk());
		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/approval")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());
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

	private void registerAttendance(Long appointmentId, boolean attended) throws Exception {
		mockMvc.perform(put("/v1/appointments/" + appointmentId + "/attendance")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"attended\": " + attended + "}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());
	}

	/** "Hoy" alcanza: a diferencia de las asignaciones, acá no hace falta un mes distinto. */
	private void createAndDeliverShipment() throws Exception {
		String response = mockMvc.perform(post("/v1/shipments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"originBranch": "San Miguel de Tucumán", "destinationBranch": "Concepción",
								 "frameIds": [%d]}
								""".formatted(FRAME_1))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		Long shipmentId = ((Number) JsonPath.read(response, "$.id")).longValue();

		mockMvc.perform(put("/v1/shipments/" + shipmentId + "/dispatch")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"trackingNumber\": \"VC0099999999\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());

		// Vía el método de dominio y no SQL directo: no hay fecha histórica que forzar acá,
		// así que no vale la pena arriesgarse a la carrera con el flush de Hibernate.
		Shipment shipment = shipmentRepository.findById(shipmentId).orElseThrow();
		shipment.applyTrackingUpdate(ShipmentStatus.DELIVERED);
		shipmentRepository.save(shipment);
	}

	@Test
	void GetImpactIndicators_AggregatesAcrossDomains() throws Exception {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime twoMonthsAgo = now.minusMonths(2);
		LocalDateTime oneMonthAgo = now.minusMonths(1);

		createAndDeliverAssignment(FRAME_1, twoMonthsAgo);
		createAndDeliverAssignment(FRAME_2, oneMonthAgo);

		Long attended = createAppointment(now.plusDays(2).withSecond(0).withNano(0));
		Long missed = createAppointment(now.plusDays(3).withSecond(0).withNano(0));
		confirmAppointment(attended);
		confirmAppointment(missed);
		registerAttendance(attended, true);
		registerAttendance(missed, false);

		createAndDeliverShipment();

		LocalDate from = now.minusMonths(3).toLocalDate();
		LocalDate to = now.plusDays(7).toLocalDate();

		mockMvc.perform(get("/v1/indicators")
						.param("from", from.format(ISO_DATE))
						.param("to", to.format(ISO_DATE))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				// Los dos marcos del template llegan "recién donados" dentro del rango.
				.andExpect(jsonPath("$.framesReceived").value(2))
				.andExpect(jsonPath("$.assignmentsDelivered").value(2))
				.andExpect(jsonPath("$.applicantsServed").value(1))
				.andExpect(jsonPath("$.appointmentsAttended").value(1))
				.andExpect(jsonPath("$.appointmentsMissed").value(1))
				.andExpect(jsonPath("$.appointmentsAttendanceRate").value(0.5))
				.andExpect(jsonPath("$.shipmentsDelivered").value(1))
				.andExpect(jsonPath("$.byMonth.length()").value(2))
				.andExpect(jsonPath("$.byMonth[0].deliveries").value(1))
				.andExpect(jsonPath("$.byMonth[0].applicants").value(1))
				.andExpect(jsonPath("$.byMonth[1].deliveries").value(1))
				.andExpect(jsonPath("$.byMonth[1].applicants").value(1));
	}

	@Test
	void GetImpactIndicators_WhenFromIsAfterTo() throws Exception {
		LocalDate from = LocalDate.now();
		LocalDate to = from.minusDays(1);

		mockMvc.perform(get("/v1/indicators")
						.param("from", from.format(ISO_DATE))
						.param("to", to.format(ISO_DATE))
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void GetImpactIndicators_WithoutRangeDefaultsToLastTwelveMonths() throws Exception {
		mockMvc.perform(get("/v1/indicators").header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.framesReceived").value(2))
				.andExpect(jsonPath("$.from").value(LocalDate.now().minusMonths(12).format(ISO_DATE)))
				.andExpect(jsonPath("$.to").value(LocalDate.now().format(ISO_DATE)));
	}
}
