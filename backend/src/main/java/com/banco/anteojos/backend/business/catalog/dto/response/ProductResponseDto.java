package com.banco.anteojos.backend.business.catalog.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;

public record ProductResponseDto(
		Long id,
		String name,
		String description,
		BigDecimal price,
		ProductStatus status,
		// Los dos en null si el producto no tiene foto cargada. imageUrl es una presigned URL que
		// vence: sirve para mostrar la foto, no para guardarla.
		String imageOriginalName,
		String imageUrl,
		LocalDateTime createdAt) {
}
