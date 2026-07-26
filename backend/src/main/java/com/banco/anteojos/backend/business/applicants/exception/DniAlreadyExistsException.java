package com.banco.anteojos.backend.business.applicants.exception;

public class DniAlreadyExistsException extends RuntimeException {

	public DniAlreadyExistsException() {
		super("Ya existe un solicitante con ese DNI");
	}
}
