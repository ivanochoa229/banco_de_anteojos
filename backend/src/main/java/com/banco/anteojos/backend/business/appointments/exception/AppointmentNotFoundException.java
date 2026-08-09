package com.banco.anteojos.backend.business.appointments.exception;

public class AppointmentNotFoundException extends RuntimeException {

	public AppointmentNotFoundException() {
		super("Turno no encontrado");
	}
}
