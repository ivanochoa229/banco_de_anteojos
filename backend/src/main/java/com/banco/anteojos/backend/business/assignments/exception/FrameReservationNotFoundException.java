package com.banco.anteojos.backend.business.assignments.exception;

public class FrameReservationNotFoundException extends RuntimeException {

	public FrameReservationNotFoundException() {
		super("No hay un marco reservado");
	}
}
