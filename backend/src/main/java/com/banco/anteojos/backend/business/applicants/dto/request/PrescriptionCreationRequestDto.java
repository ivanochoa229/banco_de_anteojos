package com.banco.anteojos.backend.business.applicants.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PrescriptionCreationRequestDto(
		@Digits(integer = 2, fraction = 2) BigDecimal rightSphere,
		@Digits(integer = 2, fraction = 2) BigDecimal rightCylinder,
		@Min(0) @Max(180) Integer rightAxis,
		@Digits(integer = 2, fraction = 2) BigDecimal leftSphere,
		@Digits(integer = 2, fraction = 2) BigDecimal leftCylinder,
		@Min(0) @Max(180) Integer leftAxis) {
}
