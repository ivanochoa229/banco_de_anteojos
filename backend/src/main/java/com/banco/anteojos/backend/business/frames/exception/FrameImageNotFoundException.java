package com.banco.anteojos.backend.business.frames.exception;

public class FrameImageNotFoundException extends RuntimeException {

	public FrameImageNotFoundException() {
		super("El marco no tiene una foto cargada");
	}
}
