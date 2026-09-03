package com.banco.anteojos.backend.business.appointments.dto.response;

import java.time.LocalDateTime;

/** URL firmada de lectura del comprobante. Vence: no sirve para guardar ni compartir. */
public record AppointmentReceiptResponseDto(
		String url,
		LocalDateTime expiresAt,
		String fileName,
		String contentType) {
}
