package com.banco.anteojos.backend.orchestrator.shipments;

import java.util.List;

import org.springframework.stereotype.Component;

import com.banco.anteojos.backend.business.frames.FrameService;
import com.banco.anteojos.backend.business.shipments.ShipmentService;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentDetailResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShipmentUseCaseHandler implements ShipmentUseCaseOrchestrator {

	private final ShipmentService shipmentService;
	private final FrameService frameService;

	@Override
	public ShipmentResponseDto createShipment(ShipmentCreationRequestDto request) {
		// Pertenencia primero: que todos los marcos del paquete existan en el inventario.
		frameService.getFrames(request.frameIds());
		return shipmentService.createShipment(request);
	}

	@Override
	public List<ShipmentResponseDto> listShipments() {
		return shipmentService.listShipments();
	}

	@Override
	public ShipmentDetailResponseDto getShipment(Long shipmentId) {
		ShipmentResponseDto shipment = shipmentService.getShipment(shipmentId);
		return shipmentService.buildDetail(shipment, frameService.getFrames(shipment.frameIds()));
	}

	@Override
	public ShipmentResponseDto dispatch(Long shipmentId, String trackingNumber) {
		return shipmentService.dispatch(shipmentId, trackingNumber);
	}

	@Override
	public ShipmentResponseDto cancel(Long shipmentId, String reason) {
		return shipmentService.cancel(shipmentId, reason);
	}

	@Override
	public void processTrackingPush(String sign, String rawBody) {
		shipmentService.processTrackingPush(sign, rawBody);
	}
}
