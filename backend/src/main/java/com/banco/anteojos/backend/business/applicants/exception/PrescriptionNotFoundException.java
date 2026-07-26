package com.banco.anteojos.backend.business.applicants.exception;

public class PrescriptionNotFoundException extends RuntimeException {

	public PrescriptionNotFoundException() {
		super("Receta no encontrada");
	}
}
