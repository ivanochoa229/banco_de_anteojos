package com.banco.anteojos.backend.business.shipments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** El número lo emite Vía Cargo al despachar; 17TRACK acepta entre 5 y 50 caracteres. */
public record ShipmentDispatchRequestDto(
		@NotBlank(message = "el número de seguimiento es obligatorio")
		@Size(min = 5, max = 50, message = "debe tener entre 5 y 50 caracteres") String trackingNumber) {
}
