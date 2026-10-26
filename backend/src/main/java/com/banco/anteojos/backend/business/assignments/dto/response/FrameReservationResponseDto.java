package com.banco.anteojos.backend.business.assignments.dto.response;

import java.time.LocalDateTime;

/** Reserva vigente de un marco. Pasado {@code expiresAt}, el marco vuelve solo al inventario. */
public record FrameReservationResponseDto(
		Long id,
		Long applicantId,
		Long frameId,
		LocalDateTime reservedAt,
		LocalDateTime expiresAt) {
}
