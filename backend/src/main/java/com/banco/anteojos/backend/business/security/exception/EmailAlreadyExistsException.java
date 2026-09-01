package com.banco.anteojos.backend.business.security.exception;

public class EmailAlreadyExistsException extends RuntimeException {

	public EmailAlreadyExistsException() {
		super("Ya existe una cuenta con ese email");
	}
}
