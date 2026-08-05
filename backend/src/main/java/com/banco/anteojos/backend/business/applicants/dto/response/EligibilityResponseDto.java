package com.banco.anteojos.backend.business.applicants.dto.response;

/**
 * Elegibilidad del beneficiario para recibir anteojos. La negativa de ANSES vigente es lo que
 * acredita que no tiene cobertura médica, que es el criterio de la fundación.
 *
 * <p>No bloquea la asignación: {@code warning} trae el motivo para que el operador lo vea y
 * decida, porque la persona suele estar en el mostrador cuando se detecta el faltante.
 */
public record EligibilityResponseDto(
		Long applicantId,
		boolean eligible,
		String warning) {
}
