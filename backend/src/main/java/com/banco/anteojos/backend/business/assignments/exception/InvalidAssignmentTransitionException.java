package com.banco.anteojos.backend.business.assignments.exception;

/**
 * La asignación existe pero su estado no admite la acción pedida (entregar algo que no volvió
 * de la óptica, tocar una asignación cancelada). Se traduce a 409, no a 400: el request está
 * bien formado, lo que no encaja es el momento del circuito.
 */
public class InvalidAssignmentTransitionException extends RuntimeException {

	public InvalidAssignmentTransitionException(String message) {
		super(message);
	}
}
