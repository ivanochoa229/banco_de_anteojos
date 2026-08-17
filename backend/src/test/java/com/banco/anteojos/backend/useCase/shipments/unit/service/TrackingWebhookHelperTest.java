package com.banco.anteojos.backend.useCase.shipments.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.banco.anteojos.backend.business.shipments.TrackingUpdate;
import com.banco.anteojos.backend.business.shipments.TrackingWebhookHelper;
import com.banco.anteojos.backend.business.shipments.exception.InvalidWebhookPayloadException;
import com.banco.anteojos.backend.business.shipments.exception.InvalidWebhookSignatureException;

import tools.jackson.databind.ObjectMapper;

@Tag("unit")
class TrackingWebhookHelperTest {

	private static final String API_KEY = "test-17track-key";

	/** Push real de ejemplo con la forma documentada en la ayuda oficial (data.number directo). */
	private static final String BODY = "{\"event\":\"TRACKING_UPDATED\",\"data\":{\"number\":\"VC0012345678\","
			+ "\"carrier\":100535,\"track_info\":{\"latest_status\":{\"status\":\"InTransit\"},"
			+ "\"latest_event\":{\"time_iso\":\"2026-08-10T09:15:00-03:00\","
			+ "\"description\":\"En viaje a destino\",\"location\":\"San Miguel de Tucumán\"}}}}";

	// SHA256 hex de BODY + "/" + API_KEY, calculado por fuera para no replicar el algoritmo acá.
	private static final String VALID_SIGN =
			"183cf12d21289c7a2b9dfc2b542a587ee8d4335aff35d7e2abe7fe410d9c2a04";

	private final TrackingWebhookHelper helper = new TrackingWebhookHelper(new ObjectMapper(), API_KEY);

	@Test
	void VerifySignature_Successful() {
		assertThatCode(() -> helper.verifySignature(BODY, VALID_SIGN)).doesNotThrowAnyException();
	}

	@Test
	void VerifySignature_WhenSignDoesNotMatch() {
		assertThatThrownBy(() -> helper.verifySignature(BODY, "a".repeat(64)))
				.isInstanceOf(InvalidWebhookSignatureException.class);
	}

	@Test
	void VerifySignature_WhenSignIsMissing() {
		assertThatThrownBy(() -> helper.verifySignature(BODY, null))
				.isInstanceOf(InvalidWebhookSignatureException.class);
	}

	@Test
	void Parse_Successful() {
		TrackingUpdate update = helper.parse(BODY).orElseThrow();

		assertThat(update.trackingNumber()).isEqualTo("VC0012345678");
		assertThat(update.rawStatus()).isEqualTo("InTransit");
		assertThat(update.description()).isEqualTo("En viaje a destino");
		assertThat(update.location()).isEqualTo("San Miguel de Tucumán");
		// El offset -03:00 se descarta: el sistema guarda LocalDateTime como todo el backend.
		assertThat(update.occurredAt()).isEqualTo(LocalDateTime.of(2026, 8, 10, 9, 15));
	}

	@Test
	void Parse_WhenPayloadUsesAcceptedArray() {
		// La documentación también muestra el push como data.accepted[] y la ubicación como objeto.
		String body = "{\"event\":\"TRACKING_UPDATED\",\"data\":{\"accepted\":[{\"number\":\"VC99\","
				+ "\"track_info\":{\"latest_status\":{\"status\":\"Delivered\"},"
				+ "\"latest_event\":{\"description\":\"Entregado\","
				+ "\"location\":{\"city\":\"Concepción\",\"country\":\"AR\"}}}}]}}";

		TrackingUpdate update = helper.parse(body).orElseThrow();

		assertThat(update.trackingNumber()).isEqualTo("VC99");
		assertThat(update.rawStatus()).isEqualTo("Delivered");
		assertThat(update.location()).isEqualTo("Concepción, AR");
		assertThat(update.occurredAt()).isNull();
	}

	@Test
	void Parse_WhenEventIsNotATrackingUpdate() {
		Optional<TrackingUpdate> update =
				helper.parse("{\"event\":\"TRACKING_STOPPED\",\"data\":{\"number\":\"VC99\"}}");

		assertThat(update).isEmpty();
	}

	@Test
	void Parse_WhenBodyIsNotJson() {
		assertThatThrownBy(() -> helper.parse("esto no es json"))
				.isInstanceOf(InvalidWebhookPayloadException.class);
	}

	@Test
	void Parse_WhenNumberIsMissing() {
		assertThatThrownBy(() -> helper.parse("{\"event\":\"TRACKING_UPDATED\",\"data\":{}}"))
				.isInstanceOf(InvalidWebhookPayloadException.class);
	}
}
