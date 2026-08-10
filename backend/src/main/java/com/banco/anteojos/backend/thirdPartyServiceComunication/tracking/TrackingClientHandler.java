package com.banco.anteojos.backend.thirdPartyServiceComunication.tracking;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class TrackingClientHandler implements TrackingClient {

	/** Vía Cargo en la lista oficial de carriers de 17TRACK (viacargo.com.ar). */
	static final int VIA_CARGO_CARRIER = 100535;

	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public TrackingClientHandler(RestClient.Builder restClientBuilder, ObjectMapper objectMapper,
			@Value("${track17.base-url:https://api.17track.net/track/v2.4}") String baseUrl,
			@Value("${track17.api-key:}") String apiKey) {
		this.restClient = restClientBuilder.baseUrl(baseUrl).defaultHeader("17token", apiKey).build();
		this.objectMapper = objectMapper;
	}

	@Override
	public void register(String trackingNumber) {
		String body;
		try {
			body = restClient.post()
					.uri("/register")
					.contentType(MediaType.APPLICATION_JSON)
					.body(List.of(Map.of("number", trackingNumber, "carrier", VIA_CARGO_CARRIER)))
					.retrieve()
					.body(String.class);
		} catch (RestClientException e) {
			throw new TrackingException(e);
		}

		// 17TRACK responde 200 aun cuando rechaza: hay que mirar code y data.rejected.
		JsonNode root = objectMapper.readTree(body == null ? "{}" : body);
		if (root.path("code").asLong(-1) != 0) {
			throw new TrackingException(null);
		}
		if (root.path("data").path("rejected").isArray()
				&& !root.path("data").path("rejected").isEmpty()) {
			throw new TrackingRejectedException();
		}
	}
}
