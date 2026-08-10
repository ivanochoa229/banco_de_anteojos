package com.banco.anteojos.backend.business.shipments.entities;

import java.util.Optional;

/**
 * PENDING y CANCELLED son nuestros (el paquete todavía no salió); REGISTERED marca el despacho
 * con el número de Vía Cargo ya registrado en 17TRACK; el resto espeja los estados que empuja
 * 17TRACK por webhook.
 */
public enum ShipmentStatus {
	PENDING,
	CANCELLED,
	REGISTERED,
	INFO_RECEIVED,
	IN_TRANSIT,
	AVAILABLE_FOR_PICKUP,
	OUT_FOR_DELIVERY,
	DELIVERED,
	DELIVERY_FAILURE,
	EXCEPTION,
	EXPIRED,
	NOT_FOUND;

	/** Mapeo desde el estado crudo de 17TRACK; vacío si el carrier inventó uno nuevo. */
	public static Optional<ShipmentStatus> fromTracking(String rawStatus) {
		return switch (rawStatus) {
			case "InfoReceived" -> Optional.of(INFO_RECEIVED);
			case "InTransit" -> Optional.of(IN_TRANSIT);
			case "AvailableForPickup" -> Optional.of(AVAILABLE_FOR_PICKUP);
			case "OutForDelivery" -> Optional.of(OUT_FOR_DELIVERY);
			case "Delivered" -> Optional.of(DELIVERED);
			case "DeliveryFailure" -> Optional.of(DELIVERY_FAILURE);
			case "Exception" -> Optional.of(EXCEPTION);
			case "Expired" -> Optional.of(EXPIRED);
			case "NotFound" -> Optional.of(NOT_FOUND);
			default -> Optional.empty();
		};
	}
}
