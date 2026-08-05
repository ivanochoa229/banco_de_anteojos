package com.banco.anteojos.backend.business.assignments.dto.request;

import jakarta.validation.constraints.Size;

public record AssignmentCancellationRequestDto(
		@Size(max = 255, message = "no puede superar los 255 caracteres") String reason) {
}
