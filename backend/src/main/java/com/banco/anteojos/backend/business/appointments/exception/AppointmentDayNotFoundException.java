package com.banco.anteojos.backend.business.appointments.exception;

public class AppointmentDayNotFoundException extends RuntimeException {

	public AppointmentDayNotFoundException() {
		super("Día de atención no encontrado");
	}
}
