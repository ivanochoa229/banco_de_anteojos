package com.banco.anteojos.backend.business.appointments.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Alta y edición de un día de atención: fecha, rango horario y duración de cada turno. La cantidad
 * de turnos no se carga: sale de cuántos turnos enteros entran entre el inicio y el fin.
 */
public record AppointmentDayRequestDto(
		@NotNull(message = "la fecha es obligatoria")
		@FutureOrPresent(message = "el día de atención no puede estar en el pasado") LocalDate date,
		@NotNull(message = "la hora de inicio es obligatoria") LocalTime startTime,
		@NotNull(message = "la hora de fin es obligatoria") LocalTime endTime,
		@NotNull(message = "la duración de cada turno es obligatoria")
		@Min(value = 5, message = "cada turno tiene que durar al menos 5 minutos")
		@Max(value = 240, message = "cada turno puede durar como máximo 240 minutos") Integer slotDurationMinutes) {
}
