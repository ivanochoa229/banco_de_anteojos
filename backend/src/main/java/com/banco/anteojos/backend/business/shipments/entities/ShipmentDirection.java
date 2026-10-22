package com.banco.anteojos.backend.business.shipments.entities;

/**
 * OUTBOUND: la fundación arma el paquete y lo manda entre sucursales. INBOUND: una donación que
 * un donante despacha por Vía Cargo hacia el depósito de la fundación.
 */
public enum ShipmentDirection {
	OUTBOUND,
	INBOUND
}
