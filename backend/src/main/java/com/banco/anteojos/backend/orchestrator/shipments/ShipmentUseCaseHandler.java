package com.banco.anteojos.backend.orchestrator.shipments;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.business.donors.DonorService;
import com.banco.anteojos.backend.business.frames.FrameService;
import com.banco.anteojos.backend.business.shipments.ShipmentService;
import com.banco.anteojos.backend.business.shipments.dto.request.InboundShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentReceptionRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentDetailResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentListItemResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentResponseDto;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentDirection;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ShipmentUseCaseHandler implements ShipmentUseCaseOrchestrator {

	private final ShipmentService shipmentService;
	private final FrameService frameService;
	private final DonorService donorService;

	@Override
	public ShipmentResponseDto createShipment(ShipmentCreationRequestDto request) {
		// Pertenencia primero: que todos los marcos del paquete existan en el inventario.
		frameService.getFrames(request.frameIds());
		return shipmentService.createShipment(request);
	}

	@Override
	public ShipmentResponseDto createInboundShipment(Long donorId, InboundShipmentCreationRequestDto request) {
		// Pertenencia primero: que el donante exista.
		donorService.getDonor(donorId);
		return shipmentService.createInboundShipment(donorId, request);
	}

	@Override
	public PageResponseDto<ShipmentListItemResponseDto> listShipments(ShipmentDirection direction, boolean onlyLive,
			Pageable pageable) {
		PageResponseDto<ShipmentResponseDto> shipments = shipmentService.listShipments(direction, onlyLive, pageable);
		return shipmentService.buildListItems(shipments,
				donorService.getNames(shipments.distinctValues(ShipmentResponseDto::donorId)));
	}

	@Override
	public PageResponseDto<ShipmentResponseDto> listShipmentsByDonor(Long donorId, Pageable pageable) {
		donorService.getDonor(donorId);
		return shipmentService.listShipmentsByDonor(donorId, pageable);
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
	public ShipmentResponseDto receive(Long shipmentId, ShipmentReceptionRequestDto request) {
		// Que todos los marcos existan; que sean del donante de la donación lo valida el service.
		return shipmentService.receive(shipmentId, frameService.getFrames(request.frameIds()));
	}

	@Override
	public void processTrackingPush(String sign, String rawBody) {
		shipmentService.processTrackingPush(sign, rawBody);
	}
}
