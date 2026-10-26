package com.banco.anteojos.backend.business.shipments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.request.InboundShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentDetailResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentListItemResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentResponseDto;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentDirection;

public interface ShipmentService {

	/** La existencia de los marcos ya la validó el orchestrator contra el dominio frames. */
	ShipmentResponseDto createShipment(ShipmentCreationRequestDto request);

	/** Donación en camino: la registra en 17TRACK antes de persistirla. El donante ya lo validó el orchestrator. */
	ShipmentResponseDto createInboundShipment(Long donorId, InboundShipmentCreationRequestDto request);

	ShipmentResponseDto getShipment(Long shipmentId);

	/** Con {@code onlyLive}, solo lo que todavía puede cambiar (ver CLOSED_STATUSES en el handler). */
	PageResponseDto<ShipmentResponseDto> listShipments(ShipmentDirection direction, boolean onlyLive,
			Pageable pageable);

	/** Las donaciones que despachó un donante. */
	PageResponseDto<ShipmentResponseDto> listShipmentsByDonor(Long donorId, Pageable pageable);

	/** Arma las filas del listado con los nombres que el orchestrator resolvió en donors. */
	PageResponseDto<ShipmentListItemResponseDto> buildListItems(PageResponseDto<ShipmentResponseDto> shipments,
			Map<Long, String> donorNames);

	/** Despacho físico: registra el número en 17TRACK antes de persistir el cambio (RF-23). */
	ShipmentResponseDto dispatch(Long shipmentId, String trackingNumber);

	ShipmentResponseDto cancel(Long shipmentId, String reason);

	/** Cierra una donación entrante con los marcos que el orchestrator ya resolvió en el dominio frames. */
	ShipmentResponseDto receive(Long shipmentId, List<FrameResponseDto> frames);

	/** Push del webhook de 17TRACK (RF-24): verifica la firma, actualiza el estado y guarda el evento. */
	void processTrackingPush(String sign, String rawBody);

	/** Arma la trazabilidad con los marcos que el orchestrator ya resolvió en el dominio frames. */
	ShipmentDetailResponseDto buildDetail(ShipmentResponseDto shipment, List<FrameResponseDto> frames);

	/** Envíos salientes completados en el rango, para el panel de indicadores (RF-26). */
	long countDelivered(LocalDateTime from, LocalDateTime to);
}
