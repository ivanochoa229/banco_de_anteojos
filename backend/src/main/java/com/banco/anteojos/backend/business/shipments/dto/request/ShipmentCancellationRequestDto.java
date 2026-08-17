package com.banco.anteojos.backend.business.shipments.dto.request;

import jakarta.validation.constraints.Size;

public record ShipmentCancellationRequestDto(
		@Size(max = 255, message = "no puede superar los 255 caracteres") String reason) {
}
