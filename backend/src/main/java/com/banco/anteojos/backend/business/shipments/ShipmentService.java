package com.banco.anteojos.backend.business.shipments;

import java.util.List;

import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentDetailResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentResponseDto;

public interface ShipmentService {

	/** La existencia de los marcos ya la validó el orchestrator contra el dominio frames. */
	ShipmentResponseDto createShipment(ShipmentCreationRequestDto request);

	ShipmentResponseDto getShipment(Long shipmentId);

	List<ShipmentResponseDto> listShipments();

	/** Despacho físico: registra el número en 17TRACK antes de persistir el cambio (RF-23). */
	ShipmentResponseDto dispatch(Long shipmentId, String trackingNumber);

	ShipmentResponseDto cancel(Long shipmentId, String reason);

	/** Push del webhook de 17TRACK (RF-24): verifica la firma, actualiza el estado y guarda el evento. */
	void processTrackingPush(String sign, String rawBody);

	/** Arma la trazabilidad con los marcos que el orchestrator ya resolvió en el dominio frames. */
	ShipmentDetailResponseDto buildDetail(ShipmentResponseDto shipment, List<FrameResponseDto> frames);
}
