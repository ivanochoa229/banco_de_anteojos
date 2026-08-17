package com.banco.anteojos.backend.business.shipments.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ShipmentResponseDto(
		Long id,
		String originBranch,
		String destinationBranch,
		String trackingNumber,
		String status,
		String notes,
		String cancellationReason,
		LocalDateTime createdAt,
		LocalDateTime dispatchedAt,
		LocalDateTime deliveredAt,
		List<Long> frameIds) {
}
