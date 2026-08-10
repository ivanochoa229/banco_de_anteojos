package com.banco.anteojos.backend.orchestrator.shipments;

import java.util.List;

import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentDetailResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentResponseDto;

public interface ShipmentUseCaseOrchestrator {

	ShipmentResponseDto createShipment(ShipmentCreationRequestDto request);

	List<ShipmentResponseDto> listShipments();

	ShipmentDetailResponseDto getShipment(Long shipmentId);

	ShipmentResponseDto dispatch(Long shipmentId, String trackingNumber);

	ShipmentResponseDto cancel(Long shipmentId, String reason);

	void processTrackingPush(String sign, String rawBody);
}
