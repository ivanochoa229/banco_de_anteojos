package com.banco.anteojos.backend.orchestrator.shipments;

import org.springframework.data.domain.Pageable;

import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.request.InboundShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentReceptionRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentDetailResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentListItemResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentResponseDto;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentDirection;

public interface ShipmentUseCaseOrchestrator {

	ShipmentResponseDto createShipment(ShipmentCreationRequestDto request);

	ShipmentResponseDto createInboundShipment(Long donorId, InboundShipmentCreationRequestDto request);

	PageResponseDto<ShipmentListItemResponseDto> listShipments(ShipmentDirection direction, boolean onlyLive,
			Pageable pageable);

	PageResponseDto<ShipmentResponseDto> listShipmentsByDonor(Long donorId, Pageable pageable);

	ShipmentDetailResponseDto getShipment(Long shipmentId);

	ShipmentResponseDto dispatch(Long shipmentId, String trackingNumber);

	ShipmentResponseDto cancel(Long shipmentId, String reason);

	ShipmentResponseDto receive(Long shipmentId, ShipmentReceptionRequestDto request);

	void processTrackingPush(String sign, String rawBody);
}
