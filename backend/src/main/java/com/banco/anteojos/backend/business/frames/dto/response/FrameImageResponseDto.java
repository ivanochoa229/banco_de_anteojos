package com.banco.anteojos.backend.business.frames.dto.response;

import java.time.LocalDateTime;

/** URL firmada de lectura de la foto del marco. Vence: no sirve para guardar ni compartir. */
public record FrameImageResponseDto(
		String url,
		LocalDateTime expiresAt,
		String fileName,
		String contentType) {
}
