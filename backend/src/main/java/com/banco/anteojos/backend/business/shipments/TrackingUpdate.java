package com.banco.anteojos.backend.business.shipments;

import java.time.LocalDateTime;

/** Lo que nos importa de un push de 17TRACK, ya parseado por {@link TrackingWebhookHelper}. */
public record TrackingUpdate(
		String trackingNumber,
		String rawStatus,
		String description,
		String location,
		LocalDateTime occurredAt) {
}
