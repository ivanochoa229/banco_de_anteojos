package com.banco.anteojos.backend.business.applicants.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Incluye la presigned URL del PDF para que el operador pueda verlo sin otro request.
 * La URL vence ({@code fileUrlExpiresAt}): no debe persistirse del lado del cliente.
 */
public record AnsesCertificateResponseDto(
		Long id,
		Long applicantId,
		String cuil,
		String transactionNumber,
		LocalDate issueDate,
		String fileOriginalName,
		String fileUrl,
		LocalDateTime fileUrlExpiresAt,
		LocalDateTime createdAt) {
}
