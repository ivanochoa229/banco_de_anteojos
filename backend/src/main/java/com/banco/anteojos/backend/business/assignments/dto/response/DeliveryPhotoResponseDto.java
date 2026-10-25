package com.banco.anteojos.backend.business.assignments.dto.response;

import java.time.LocalDateTime;

/** URL firmada de lectura de la foto de la entrega. Vence: no sirve para guardar ni compartir. */
public record DeliveryPhotoResponseDto(
		String url,
		LocalDateTime expiresAt,
		String fileName,
		String contentType) {
}
