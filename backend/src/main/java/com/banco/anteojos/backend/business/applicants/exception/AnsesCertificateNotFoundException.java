package com.banco.anteojos.backend.business.applicants.exception;

public class AnsesCertificateNotFoundException extends RuntimeException {

	public AnsesCertificateNotFoundException() {
		super("El solicitante no tiene una certificación negativa de ANSES cargada");
	}
}
