package com.banco.anteojos.backend.business.frames.exception;

/**
 * El marco existe pero su estado no admite la transición pedida (asignar uno que ya está
 * asignado, entregar uno que sigue en la óptica). Se traduce a 409.
 */
public class InvalidFrameTransitionException extends RuntimeException {

	public InvalidFrameTransitionException(String message) {
		super(message);
	}
}
