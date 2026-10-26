package com.banco.anteojos.backend.business.assignments.dto.response;

/**
 * Fila de la cola de asignaciones: la asignación con lo que hace falta para reconocerla sin abrirla.
 * Los nombres vienen null si el beneficiario o el marco ya no existen.
 */
public record AssignmentListItemResponseDto(
		AssignmentResponseDto assignment,
		String applicantName,
		String frameSealCode) {
}
