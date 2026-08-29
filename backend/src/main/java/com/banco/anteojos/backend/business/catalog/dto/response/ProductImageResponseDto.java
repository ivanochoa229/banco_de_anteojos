package com.banco.anteojos.backend.business.catalog.dto.response;

import java.time.LocalDateTime;

/** URL firmada de lectura de la foto del producto. Vence: no sirve para guardar ni compartir. */
public record ProductImageResponseDto(
		String url,
		LocalDateTime expiresAt,
		String fileName,
		String contentType) {
}
