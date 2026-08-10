package com.banco.anteojos.backend.business.shipments.exception;

public class ShipmentNotFoundException extends RuntimeException {

	public ShipmentNotFoundException() {
		super("Envío no encontrado");
	}
}
