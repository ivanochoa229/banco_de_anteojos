package com.banco.anteojos.backend.business.shipments.exception;

public class FrameFromAnotherDonorException extends RuntimeException {

	public FrameFromAnotherDonorException() {
		super("Hay marcos cargados a otro donante: no pueden haber llegado en esta donación");
	}
}
