package com.banco.anteojos.backend.business.security.exception;

public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("Credenciales inválidas");
	}
}
