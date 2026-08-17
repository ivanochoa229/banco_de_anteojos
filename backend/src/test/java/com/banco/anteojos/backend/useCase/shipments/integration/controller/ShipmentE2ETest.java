package com.banco.anteojos.backend.useCase.shipments.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

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

import com.banco.anteojos.backend.business.security.JwtService;
import com.banco.anteojos.backend.business.security.entities.Role;
import com.banco.anteojos.backend.business.security.entities.User;
import com.banco.anteojos.backend.business.shipments.entities.Shipment;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentStatus;
import com.banco.anteojos.backend.persistence.shipments.ShipmentEventPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.shipments.ShipmentPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;
import com.banco.anteojos.backend.thirdPartyServiceComunication.tracking.TrackingClient;
import com.banco.anteojos.backend.thirdPartyServiceComunication.tracking.TrackingException;
import com.jayway.jsonpath.JsonPath;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Envíos con los marcos del template (400000 y 400001). El cliente HTTP de 17TRACK se mockea;
 * el webhook usa la verificación de firma real con la API key fija del perfil de test.
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/frames/frames_template.sql")
class ShipmentE2ETest {

	private static final String TEST_API_KEY = "test-17track-key";
	private static final String TRACKING_NUMBER = "VC0012345678";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private ShipmentPostgresSqlRepository shipmentRepository;

	@Autowired
	private ShipmentEventPostgresSqlRepository eventRepository;

	@MockitoBean
	private TrackingClient trackingClient;

	@MockitoBean
	private R2StorageClient r2StorageClient;

	@PersistenceContext
	private EntityManager entityManager;

	private String bearerToken() {
		return "Bearer " + jwtService.generateToken(
				new User("Operador Test", "operator.test@bancoanteojos.org", "hash", Role.OPERATOR));
	}

	private String creationBody() {
		return """
				{"originBranch": "San Miguel de Tucumán", "destinationBranch": "Concepción",
				 "frameIds": [400000, 400001], "notes": "frágil"}
				""";
	}

	private Long createShipment() throws Exception {
		String response = mockMvc.perform(post("/v1/shipments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(response, "$.id")).longValue();
	}

	private Long createDispatchedShipment() throws Exception {
		Long shipmentId = createShipment();
		mockMvc.perform(put("/v1/shipments/" + shipmentId + "/dispatch")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"trackingNumber\": \"" + TRACKING_NUMBER + "\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk());
		return shipmentId;
	}

	private String pushBody(String status, String description) {
		return "{\"event\":\"TRACKING_UPDATED\",\"data\":{\"number\":\"" + TRACKING_NUMBER + "\","
				+ "\"carrier\":100535,\"track_info\":{\"latest_status\":{\"status\":\"" + status + "\"},"
				+ "\"latest_event\":{\"time_iso\":\"2026-08-10T09:15:00-03:00\","
				+ "\"description\":\"" + description + "\",\"location\":\"Tucumán\"}}}}";
	}

	/** Misma regla que usa 17TRACK para firmar: SHA256 hex de cuerpo + "/" + API key. */
	private String sign(String body) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		return HexFormat.of().formatHex(
				digest.digest((body + "/" + TEST_API_KEY).getBytes(StandardCharsets.UTF_8)));
	}

	private Shipment shipmentInDb(Long shipmentId) {
		return shipmentRepository.findById(shipmentId).orElseThrow();
	}

	@Test
	void CreateShipment_Successful() throws Exception {
		mockMvc.perform(post("/v1/shipments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody())
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.trackingNumber").isEmpty())
				.andExpect(jsonPath("$.frameIds.length()").value(2));
	}

	@Test
	void CreateShipment_WhenAFrameDoesNotExist() throws Exception {
		mockMvc.perform(post("/v1/shipments")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"originBranch": "Tucumán", "destinationBranch": "Concepción",
								 "frameIds": [400000, 999999]}
								""")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Marco no encontrado"));

		assertThat(shipmentRepository.count()).isZero();
	}

	@Test
	void Dispatch_Successful() throws Exception {
		Long shipmentId = createShipment();

		mockMvc.perform(put("/v1/shipments/" + shipmentId + "/dispatch")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"trackingNumber\": \"" + TRACKING_NUMBER + "\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REGISTERED"))
				.andExpect(jsonPath("$.dispatchedAt").isNotEmpty());

		// RF-23: el alta en 17TRACK es lo que hace que después lleguen los pushes.
		verify(trackingClient).register(TRACKING_NUMBER);
	}

	@Test
	void Dispatch_WhenAlreadyDispatched() throws Exception {
		Long shipmentId = createDispatchedShipment();

		mockMvc.perform(put("/v1/shipments/" + shipmentId + "/dispatch")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"trackingNumber\": \"VC0099999999\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isConflict());
	}

	@Test
	void Dispatch_WhenTrackingServiceIsDown() throws Exception {
		Long shipmentId = createShipment();
		doThrow(new TrackingException(null)).when(trackingClient).register(TRACKING_NUMBER);

		mockMvc.perform(put("/v1/shipments/" + shipmentId + "/dispatch")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"trackingNumber\": \"" + TRACKING_NUMBER + "\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isServiceUnavailable());

		// En producción la entidad queda detached y el cambio muere con el 503; acá el test
		// comparte transacción con el request y la deja managed, así que hay que descartar el
		// persistence context para leer lo que de verdad llegó a la DB.
		entityManager.clear();

		// El despacho no se persistió: el operador reintenta cuando 17TRACK vuelva.
		assertThat(shipmentInDb(shipmentId).getStatus()).isEqualTo(ShipmentStatus.PENDING);
	}

	@Test
	void Webhook_UpdatesStatusAndRecordsEvent() throws Exception {
		Long shipmentId = createDispatchedShipment();
		String body = pushBody("InTransit", "En viaje a destino");

		// Sin token: el webhook es público y lo autentica la firma.
		mockMvc.perform(post("/v1/shipments/webhook")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body)
						.header("sign", sign(body)))
				.andExpect(status().isOk());

		assertThat(shipmentInDb(shipmentId).getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
		assertThat(eventRepository.findByShipmentIdOrderByIdAsc(shipmentId)).hasSize(1);
	}

	@Test
	void Webhook_WhenDelivered() throws Exception {
		Long shipmentId = createDispatchedShipment();
		String body = pushBody("Delivered", "Entregado en sucursal");

		mockMvc.perform(post("/v1/shipments/webhook")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body)
						.header("sign", sign(body)))
				.andExpect(status().isOk());

		Shipment shipment = shipmentInDb(shipmentId);
		assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
		assertThat(shipment.getDeliveredAt()).isNotNull();
	}

	@Test
	void Webhook_WhenSignatureIsInvalid() throws Exception {
		Long shipmentId = createDispatchedShipment();
		String body = pushBody("Delivered", "Entregado");

		mockMvc.perform(post("/v1/shipments/webhook")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body)
						.header("sign", "f".repeat(64)))
				.andExpect(status().isUnauthorized());

		assertThat(shipmentInDb(shipmentId).getStatus()).isEqualTo(ShipmentStatus.REGISTERED);
	}

	@Test
	void Webhook_WhenTrackingNumberIsUnknown() throws Exception {
		String body = pushBody("InTransit", "En viaje").replace(TRACKING_NUMBER, "VC0000000404");

		// 200 igual: un error haría que 17TRACK reintente un push inaplicable para siempre.
		mockMvc.perform(post("/v1/shipments/webhook")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body)
						.header("sign", sign(body)))
				.andExpect(status().isOk());
	}

	@Test
	void GetShipment_ReturnsTraceability() throws Exception {
		Long shipmentId = createDispatchedShipment();
		String body = pushBody("InTransit", "En viaje a destino");
		mockMvc.perform(post("/v1/shipments/webhook")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body)
						.header("sign", sign(body)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/v1/shipments/" + shipmentId)
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.shipment.status").value("IN_TRANSIT"))
				.andExpect(jsonPath("$.frames.length()").value(2))
				.andExpect(jsonPath("$.frames[0].sealCode").value("A-1001"))
				.andExpect(jsonPath("$.events.length()").value(1))
				.andExpect(jsonPath("$.events[0].status").value("InTransit"))
				.andExpect(jsonPath("$.events[0].description").value("En viaje a destino"));
	}

	@Test
	void Cancel_Successful() throws Exception {
		Long shipmentId = createShipment();

		mockMvc.perform(put("/v1/shipments/" + shipmentId + "/cancellation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"reason\": \"faltó un marco\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CANCELLED"))
				.andExpect(jsonPath("$.cancellationReason").value("faltó un marco"));
	}

	@Test
	void Cancel_WhenAlreadyDispatched() throws Exception {
		Long shipmentId = createDispatchedShipment();

		mockMvc.perform(put("/v1/shipments/" + shipmentId + "/cancellation")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"reason\": \"tarde\"}")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("El envío ya fue despachado"));
	}

	@Test
	void ListShipments_Successful() throws Exception {
		createShipment();

		mockMvc.perform(get("/v1/shipments")
						.header(HttpHeaders.AUTHORIZATION, bearerToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].destinationBranch").value("Concepción"));
	}

	@Test
	void CreateShipment_WithoutToken() throws Exception {
		mockMvc.perform(post("/v1/shipments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(creationBody()))
				.andExpect(status().isUnauthorized());
	}
}
