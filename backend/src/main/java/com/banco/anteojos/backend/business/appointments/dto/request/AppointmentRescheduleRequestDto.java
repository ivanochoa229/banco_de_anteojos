package com.banco.anteojos.backend.business.appointments.dto.request;

import jakarta.validation.constraints.NotNull;

/** Igual que al crearlo: se elige el nuevo día y el backend asigna la primera franja libre. */
public record AppointmentRescheduleRequestDto(
		@NotNull(message = "el nuevo día de atención es obligatorio") Long appointmentDayId) {
}
