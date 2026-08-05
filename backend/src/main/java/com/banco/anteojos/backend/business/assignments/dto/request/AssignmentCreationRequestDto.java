package com.banco.anteojos.backend.business.assignments.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssignmentCreationRequestDto(
		@NotNull(message = "el marco es obligatorio") Long frameId,
		@NotNull(message = "la receta es obligatoria") Long prescriptionId,
		@Size(max = 500, message = "no puede superar los 500 caracteres") String notes) {
}
