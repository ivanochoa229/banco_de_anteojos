package com.banco.anteojos.backend.business.assignments.dto.response;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Ids de los usuarios que registraron cada hito; null si el hito no ocurrió o es anterior a la auditoría. */
public record AssignmentResponsibleIdsDto(
		Long assignedBy,
		Long sentToOpticianBy,
		Long returnedBy,
		Long deliveredBy,
		Long cancelledBy) {

	public Set<Long> distinctValues() {
		return Stream.of(assignedBy, sentToOpticianBy, returnedBy, deliveredBy, cancelledBy)
				.filter(Objects::nonNull).collect(Collectors.toSet());
	}
}
