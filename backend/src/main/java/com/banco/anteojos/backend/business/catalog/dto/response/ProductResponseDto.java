package com.banco.anteojos.backend.business.catalog.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;

public record ProductResponseDto(
		Long id,
		String name,
		String description,
		BigDecimal price,
		Integer stockQuantity,
		ProductStatus status,
		// null si el producto no tiene foto cargada.
		String imageOriginalName,
		LocalDateTime createdAt) {
}
