package com.banco.anteojos.backend.business.assignments.dto.response;

import java.time.LocalDateTime;

/** Cada timestamp en null es un hito del circuito que todavía no ocurrió. */
public record AssignmentResponseDto(
		Long id,
		Long applicantId,
		Long frameId,
		Long prescriptionId,
		LocalDateTime assignedAt,
		LocalDateTime sentToOpticianAt,
		LocalDateTime returnedAt,
		LocalDateTime deliveredAt,
		LocalDateTime cancelledAt,
		String cancellationReason,
		String notes) {
}
