package com.banco.anteojos.backend.business.shipments.dto.response;

import java.util.List;

import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;

/** Trazabilidad del envío (RF-25): el paquete, los marcos que lleva y el historial del carrier. */
public record ShipmentDetailResponseDto(
		ShipmentResponseDto shipment,
		List<FrameResponseDto> frames,
		List<ShipmentEventResponseDto> events) {
}
