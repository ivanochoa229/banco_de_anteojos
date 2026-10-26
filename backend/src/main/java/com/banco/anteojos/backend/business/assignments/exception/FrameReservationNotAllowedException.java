package com.banco.anteojos.backend.business.assignments.exception;

/**
 * El beneficiario todavía no puede reservar (le falta la negativa de ANSES vigente o la receta,
 * o ya tiene otra reserva), o la reserva ya terminó. Se traduce a 409: el pedido está bien
 * formado, lo que no encaja es el momento.
 */
public class FrameReservationNotAllowedException extends RuntimeException {

	public FrameReservationNotAllowedException(String message) {
		super(message);
	}
}
