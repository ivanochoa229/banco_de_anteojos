package com.banco.anteojos.backend.business.shipments.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/** Saliente: con sucursales y sin donante. Entrante: con donante y sin sucursales. */
public record ShipmentResponseDto(
		Long id,
		String direction,
		Long donorId,
		String originBranch,
		String destinationBranch,
		String trackingNumber,
		String status,
		String notes,
		String cancellationReason,
		LocalDateTime createdAt,
		LocalDateTime dispatchedAt,
		LocalDateTime deliveredAt,
		LocalDateTime receivedAt,
		List<Long> frameIds) {
}
