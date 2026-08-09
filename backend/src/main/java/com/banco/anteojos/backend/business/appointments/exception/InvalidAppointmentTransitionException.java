package com.banco.anteojos.backend.business.appointments.exception;

public class InvalidAppointmentTransitionException extends RuntimeException {

	public InvalidAppointmentTransitionException(String message) {
		super(message);
	}
}
