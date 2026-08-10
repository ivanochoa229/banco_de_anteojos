package com.banco.anteojos.backend.business.shipments.exception;

public class TrackingNumberAlreadyExistsException extends RuntimeException {

	public TrackingNumberAlreadyExistsException() {
		super("Ya existe un envío con ese número de seguimiento");
	}
}
