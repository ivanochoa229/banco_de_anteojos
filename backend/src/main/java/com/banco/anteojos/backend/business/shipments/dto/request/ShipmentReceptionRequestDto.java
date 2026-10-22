package com.banco.anteojos.backend.business.shipments.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/**
 * Los marcos que traía la donación, ya cargados al inventario con su precinto. Puede ir vacía:
 * el paquete pudo no traer nada aprovechable.
 */
public record ShipmentReceptionRequestDto(
		@NotNull(message = "indicá los marcos recibidos (puede ser una lista vacía)") List<Long> frameIds) {
}
