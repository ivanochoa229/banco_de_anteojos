package com.banco.anteojos.backend.thirdPartyServiceComunication.tracking;

public class TrackingRejectedException extends RuntimeException {

	public TrackingRejectedException() {
		super("17TRACK rechazó el número de seguimiento: revisá que sea el que emitió Vía Cargo");
	}
}
