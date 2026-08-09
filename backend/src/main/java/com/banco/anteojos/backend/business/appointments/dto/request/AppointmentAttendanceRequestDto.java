package com.banco.anteojos.backend.business.appointments.dto.request;

import jakarta.validation.constraints.NotNull;

public record AppointmentAttendanceRequestDto(
		@NotNull(message = "hay que indicar si el beneficiario asistió") Boolean attended) {
}
