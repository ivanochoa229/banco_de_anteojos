package com.banco.anteojos.backend.business.shipments.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ShipmentCreationRequestDto(
		@NotBlank(message = "la sucursal de origen es obligatoria")
		@Size(max = 120, message = "no puede superar los 120 caracteres") String originBranch,
		@NotBlank(message = "la sucursal de destino es obligatoria")
		@Size(max = 120, message = "no puede superar los 120 caracteres") String destinationBranch,
		@NotEmpty(message = "el paquete tiene que llevar al menos un marco") List<Long> frameIds,
		@Size(max = 500, message = "no puede superar los 500 caracteres") String notes) {
}
