package com.banco.anteojos.backend.business.applicants.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PrescriptionResponseDto(
		Long id,
		Long applicantId,
		BigDecimal rightSphere,
		BigDecimal rightCylinder,
		Integer rightAxis,
		BigDecimal leftSphere,
		BigDecimal leftCylinder,
		Integer leftAxis,
		LocalDateTime createdAt) {
}
