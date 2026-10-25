package com.banco.anteojos.backend.business.assignments.exception;

public class DeliveryPhotoNotFoundException extends RuntimeException {

	public DeliveryPhotoNotFoundException() {
		super("La entrega no tiene una foto cargada");
	}
}
