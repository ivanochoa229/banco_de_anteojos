package com.banco.anteojos.backend.thirdPartyServiceComunication.tracking;

public class TrackingException extends RuntimeException {

	public TrackingException(Throwable cause) {
		super("No se pudo registrar el envío en el seguimiento, intentá de nuevo", cause);
	}
}
