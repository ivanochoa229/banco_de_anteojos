package com.banco.anteojos.backend.business.shipments.dto.response;

import java.time.LocalDateTime;

/** {@code status} es el valor crudo del carrier: la trazabilidad muestra lo que informó él. */
public record ShipmentEventResponseDto(
		String status,
		String description,
		String location,
		LocalDateTime occurredAt,
		LocalDateTime receivedAt) {
}
