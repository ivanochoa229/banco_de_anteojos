package com.banco.anteojos.backend.business.appointments.dto.response;

import java.time.LocalDateTime;

public record AppointmentResponseDto(
		Long id,
		Long applicantId,
		Long assignmentId,
		LocalDateTime scheduledAt,
		String status,
		String notes,
		String cancellationReason,
		LocalDateTime createdAt,
		String receiptOriginalName) {
}
