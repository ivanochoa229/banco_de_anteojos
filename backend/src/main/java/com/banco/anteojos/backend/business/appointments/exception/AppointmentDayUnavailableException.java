package com.banco.anteojos.backend.business.appointments.exception;

/** El día existe pero ya no se puede reservar en él: sin franjas libres o ya pasado. */
public class AppointmentDayUnavailableException extends RuntimeException {

	public AppointmentDayUnavailableException(String message) {
		super(message);
	}
}
