package com.banco.anteojos.backend.business.assignments.dto.response;

/**
 * Respuesta del alta. La advertencia de elegibilidad viaja solo acá y no en
 * {@link AssignmentResponseDto}: se evalúa en el momento de asignar y no se recalcula después,
 * así que devolverla en las consultas haría parecer elegible a quien nunca se verificó.
 *
 * <p>{@code eligibilityWarning} en null significa que el beneficiario tenía la negativa de ANSES
 * vigente al asignarle el marco. No bloquea: la decisión final es del operador.
 */
public record AssignmentCreationResponseDto(
		AssignmentResponseDto assignment,
		String eligibilityWarning) {
}
