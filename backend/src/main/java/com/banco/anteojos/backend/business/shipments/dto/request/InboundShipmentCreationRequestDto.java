package com.banco.anteojos.backend.business.shipments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Donación en camino. El número es la "Guía No." del comprobante que Vía Cargo le da al donante
 * (no el remito): es el que reconoce el seguimiento.
 */
public record InboundShipmentCreationRequestDto(
		@NotBlank(message = "el número de guía es obligatorio")
		@Size(min = 5, max = 50, message = "debe tener entre 5 y 50 caracteres") String trackingNumber,
		@Size(max = 500, message = "no puede superar los 500 caracteres") String notes) {
}
