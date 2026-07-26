package com.banco.anteojos.backend.business.applicants.dto.response;

import java.time.LocalDateTime;

/** URL firmada de lectura del archivo de la receta. Vence: no sirve para guardar ni compartir. */
public record PrescriptionFileResponseDto(
		String url,
		LocalDateTime expiresAt,
		String fileName,
		String contentType) {
}
