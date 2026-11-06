package com.banco.anteojos.backend.business.assignments.dto.response;

/** Nombre de quien registró cada hito (RF-16 / RNF-09). Null si no hay dato: el hito no ocurrió o es previo a la auditoría. */
public record AssignmentResponsiblesDto(
		String assignedBy,
		String sentToOpticianBy,
		String returnedBy,
		String deliveredBy,
		String cancelledBy) {
}
