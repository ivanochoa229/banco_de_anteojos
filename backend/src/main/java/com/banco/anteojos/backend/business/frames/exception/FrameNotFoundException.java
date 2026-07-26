package com.banco.anteojos.backend.business.frames.exception;

public class FrameNotFoundException extends RuntimeException {

	public FrameNotFoundException() {
		super("Marco no encontrado");
	}
}
