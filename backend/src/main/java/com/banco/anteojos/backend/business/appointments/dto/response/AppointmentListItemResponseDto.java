package com.banco.anteojos.backend.business.appointments.dto.response;

/** Fila de la agenda: el turno con el nombre del beneficiario (null si ya no existe). */
public record AppointmentListItemResponseDto(
		AppointmentResponseDto appointment,
		String applicantName) {
}
