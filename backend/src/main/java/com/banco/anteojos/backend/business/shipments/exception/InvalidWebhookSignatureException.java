package com.banco.anteojos.backend.business.shipments.exception;

public class InvalidWebhookSignatureException extends RuntimeException {

	public InvalidWebhookSignatureException() {
		super("Firma del webhook inválida");
	}
}
