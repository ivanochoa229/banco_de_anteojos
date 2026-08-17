package com.banco.anteojos.backend.thirdPartyServiceComunication.tracking;

/**
 * Cliente de 17TRACK (RF-23). Solo el alta del número: las actualizaciones no se consultan,
 * llegan solas por el webhook push (decisión de integración, tier gratuito de 100 envíos/mes).
 */
public interface TrackingClient {

	void register(String trackingNumber);
}
