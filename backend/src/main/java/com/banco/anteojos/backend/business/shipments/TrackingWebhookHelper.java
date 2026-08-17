package com.banco.anteojos.backend.business.shipments;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.banco.anteojos.backend.business.shipments.exception.InvalidWebhookPayloadException;
import com.banco.anteojos.backend.business.shipments.exception.InvalidWebhookSignatureException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Verificación y parseo del push de 17TRACK. Es procesamiento local (mismo criterio que la
 * negativa de ANSES): acá no se le pega a ninguna API, por eso vive en business y no en la capa
 * de terceros.
 */
@Component
public class TrackingWebhookHelper {

	private final ObjectMapper objectMapper;
	private final String apiKey;

	public TrackingWebhookHelper(ObjectMapper objectMapper, @Value("${track17.api-key:}") String apiKey) {
		this.objectMapper = objectMapper;
		this.apiKey = apiKey;
	}

	/** 17TRACK firma cada push con SHA256 hex de {@code cuerpoCrudo + "/" + apiKey} en el header sign. */
	public void verifySignature(String rawBody, String sign) {
		if (sign == null || sign.isBlank()) {
			throw new InvalidWebhookSignatureException();
		}
		String expected = sha256Hex(rawBody + "/" + apiKey);
		// Comparación en tiempo constante: la firma es lo único que autentica este endpoint público.
		if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
				sign.toLowerCase().getBytes(StandardCharsets.UTF_8))) {
			throw new InvalidWebhookSignatureException();
		}
	}

	/**
	 * Vacío si el evento no es una actualización de tracking (17TRACK también avisa cuando deja
	 * de seguir un número). La documentación muestra el payload con {@code data.number} directo y
	 * también como {@code data.accepted[]}: se aceptan las dos formas a propósito.
	 */
	public Optional<TrackingUpdate> parse(String rawBody) {
		JsonNode root;
		try {
			root = objectMapper.readTree(rawBody);
		} catch (RuntimeException e) {
			throw new InvalidWebhookPayloadException();
		}

		if (!"TRACKING_UPDATED".equals(root.path("event").asString(null))) {
			return Optional.empty();
		}

		JsonNode data = root.path("data");
		if (data.path("accepted").isArray() && !data.path("accepted").isEmpty()) {
			data = data.path("accepted").path(0);
		}

		String number = data.path("number").asString(null);
		if (number == null || number.isBlank()) {
			throw new InvalidWebhookPayloadException();
		}

		JsonNode trackInfo = data.path("track_info");
		JsonNode latestEvent = trackInfo.path("latest_event");
		return Optional.of(new TrackingUpdate(
				number.trim(),
				trackInfo.path("latest_status").path("status").asString(""),
				latestEvent.path("description").asString(null),
				parseLocation(latestEvent.path("location")),
				parseTime(latestEvent.path("time_iso").asString(null))));
	}

	// La ubicación llega como string o como objeto {city, country} según la versión del push.
	private String parseLocation(JsonNode location) {
		if (location.isObject()) {
			String city = location.path("city").asString("");
			String country = location.path("country").asString("");
			String joined = String.join(", ", java.util.stream.Stream.of(city, country)
					.filter(part -> !part.isBlank()).toList());
			return joined.isBlank() ? null : joined;
		}
		String value = location.asString(null);
		return value == null || value.isBlank() ? null : value;
	}

	/** El carrier manda ISO con o sin offset; una fecha rota no invalida el push, queda en null. */
	private LocalDateTime parseTime(String timeIso) {
		if (timeIso == null || timeIso.isBlank()) return null;
		try {
			return OffsetDateTime.parse(timeIso).toLocalDateTime();
		} catch (DateTimeParseException e) {
			try {
				return LocalDateTime.parse(timeIso);
			} catch (DateTimeParseException ignored) {
				return null;
			}
		}
	}

	private String sha256Hex(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			// SHA-256 viene con el JDK: si falta, el runtime está roto.
			throw new IllegalStateException(e);
		}
	}
}
