package com.banco.anteojos.backend.business.shipments.exception;

public class InvalidWebhookPayloadException extends RuntimeException {

	public InvalidWebhookPayloadException() {
		super("Cuerpo del webhook inválido");
	}
}
