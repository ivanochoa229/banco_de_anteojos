package com.banco.anteojos.backend.business.appointments.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Alta y edición de un día de atención: fecha, hora del primer turno, duración y cupo. */
public record AppointmentDayRequestDto(
		@NotNull(message = "la fecha es obligatoria")
		@FutureOrPresent(message = "el día de atención no puede estar en el pasado") LocalDate date,
		@NotNull(message = "la hora de inicio es obligatoria") LocalTime startTime,
		@NotNull(message = "la duración de cada turno es obligatoria")
		@Min(value = 5, message = "cada turno tiene que durar al menos 5 minutos")
		@Max(value = 240, message = "cada turno puede durar como máximo 240 minutos") Integer slotDurationMinutes,
		@NotNull(message = "la cantidad de turnos es obligatoria")
		@Min(value = 1, message = "tiene que haber al menos un turno")
		@Max(value = 200, message = "no puede haber más de 200 turnos en un día") Integer slotCount) {
}
