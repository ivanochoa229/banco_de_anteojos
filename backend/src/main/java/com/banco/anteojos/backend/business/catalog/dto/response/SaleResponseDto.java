package com.banco.anteojos.backend.business.catalog.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SaleResponseDto(
		Long id,
		Long productId,
		BigDecimal price,
		String buyerName,
		String notes,
		LocalDateTime soldAt) {
}
