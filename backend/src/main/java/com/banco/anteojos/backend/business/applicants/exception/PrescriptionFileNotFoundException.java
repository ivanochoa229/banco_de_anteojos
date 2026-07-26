package com.banco.anteojos.backend.business.applicants.exception;

public class PrescriptionFileNotFoundException extends RuntimeException {

	public PrescriptionFileNotFoundException() {
		super("La receta no tiene un archivo adjunto");
	}
}
