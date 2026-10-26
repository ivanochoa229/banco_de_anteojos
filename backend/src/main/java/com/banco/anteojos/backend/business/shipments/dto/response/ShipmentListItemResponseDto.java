package com.banco.anteojos.backend.business.shipments.dto.response;

/** Fila del listado de envíos. {@code donorName} solo en las donaciones entrantes (y null si el donante ya no existe). */
public record ShipmentListItemResponseDto(
		ShipmentResponseDto shipment,
		String donorName) {
}
