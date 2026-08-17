package com.banco.anteojos.backend.business.shipments.exception;

public class InvalidShipmentTransitionException extends RuntimeException {

	public InvalidShipmentTransitionException(String message) {
		super(message);
	}
}
