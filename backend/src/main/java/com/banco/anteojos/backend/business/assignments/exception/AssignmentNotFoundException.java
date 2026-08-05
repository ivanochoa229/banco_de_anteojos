package com.banco.anteojos.backend.business.assignments.exception;

public class AssignmentNotFoundException extends RuntimeException {

	public AssignmentNotFoundException() {
		super("Asignación no encontrada");
	}
}
