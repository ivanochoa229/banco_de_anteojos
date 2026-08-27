package com.banco.anteojos.backend.business.catalog.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaleResponseDto(
		Long id,
		Long productId,
		Integer quantity,
		BigDecimal unitPrice,
		BigDecimal totalAmount,
		String buyerName,
		String notes,
		LocalDateTime soldAt) {
}
