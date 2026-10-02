package com.banco.anteojos.backend.business.appointments.exception;

/** La configuración pedida para el día choca con su estado actual (turnos ya dados, fecha repetida). */
public class InvalidAppointmentDayException extends RuntimeException {

	public InvalidAppointmentDayException(String message) {
		super(message);
	}
}
