package com.banco.anteojos.backend.business.appointments.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record AppointmentRescheduleRequestDto(
		@NotNull(message = "la nueva fecha del turno es obligatoria")
		@Future(message = "el turno debe ser en el futuro") LocalDateTime scheduledAt) {
}
