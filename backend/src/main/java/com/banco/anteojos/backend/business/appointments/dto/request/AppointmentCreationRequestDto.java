package com.banco.anteojos.backend.business.appointments.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** {@code assignmentId} solo viaja cuando es un turno de retiro del par de anteojos terminado. */
public record AppointmentCreationRequestDto(
		@NotNull(message = "la fecha del turno es obligatoria")
		@Future(message = "el turno debe ser en el futuro") LocalDateTime scheduledAt,
		Long assignmentId,
		@Size(max = 500, message = "no puede superar los 500 caracteres") String notes) {
}
