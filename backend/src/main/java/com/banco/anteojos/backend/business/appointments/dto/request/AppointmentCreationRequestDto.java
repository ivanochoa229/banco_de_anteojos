package com.banco.anteojos.backend.business.appointments.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Se elige el día de atención, no la hora: la franja la asigna el backend (la primera libre).
 * {@code assignmentId} solo viaja cuando es un turno de retiro del par de anteojos terminado.
 */
public record AppointmentCreationRequestDto(
		@NotNull(message = "el día de atención es obligatorio") Long appointmentDayId,
		Long assignmentId,
		@Size(max = 500, message = "no puede superar los 500 caracteres") String notes) {
}
