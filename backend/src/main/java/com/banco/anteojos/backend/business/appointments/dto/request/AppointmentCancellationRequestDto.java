package com.banco.anteojos.backend.business.appointments.dto.request;

import jakarta.validation.constraints.Size;

public record AppointmentCancellationRequestDto(
		@Size(max = 255, message = "no puede superar los 255 caracteres") String reason) {
}
