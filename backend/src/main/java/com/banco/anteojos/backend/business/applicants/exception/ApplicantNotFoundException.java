package com.banco.anteojos.backend.business.applicants.exception;

public class ApplicantNotFoundException extends RuntimeException {

	public ApplicantNotFoundException() {
		super("Solicitante no encontrado");
	}
}
