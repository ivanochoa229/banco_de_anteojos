package com.banco.anteojos.backend.business.frames.exception;

public class SealCodeAlreadyExistsException extends RuntimeException {

	public SealCodeAlreadyExistsException() {
		super("Ya existe un marco con ese precinto");
	}
}
