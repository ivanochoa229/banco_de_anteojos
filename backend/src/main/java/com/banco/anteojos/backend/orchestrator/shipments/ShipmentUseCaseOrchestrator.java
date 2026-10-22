package com.banco.anteojos.backend.orchestrator.shipments;

import java.util.List;

import com.banco.anteojos.backend.business.shipments.dto.request.InboundShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentReceptionRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentDetailResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentResponseDto;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentDirection;

public interface ShipmentUseCaseOrchestrator {

	ShipmentResponseDto createShipment(ShipmentCreationRequestDto request);

	ShipmentResponseDto createInboundShipment(Long donorId, InboundShipmentCreationRequestDto request);

	List<ShipmentResponseDto> listShipments(ShipmentDirection direction);

	ShipmentDetailResponseDto getShipment(Long shipmentId);

	ShipmentResponseDto dispatch(Long shipmentId, String trackingNumber);

	ShipmentResponseDto cancel(Long shipmentId, String reason);

	ShipmentResponseDto receive(Long shipmentId, ShipmentReceptionRequestDto request);

	void processTrackingPush(String sign, String rawBody);
}
