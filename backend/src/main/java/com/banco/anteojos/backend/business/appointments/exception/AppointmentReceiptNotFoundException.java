package com.banco.anteojos.backend.business.appointments.exception;

public class AppointmentReceiptNotFoundException extends RuntimeException {

	public AppointmentReceiptNotFoundException() {
		super("El turno no tiene un comprobante adjunto");
	}
}
