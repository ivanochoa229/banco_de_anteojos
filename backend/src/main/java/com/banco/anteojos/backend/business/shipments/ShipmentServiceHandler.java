package com.banco.anteojos.backend.business.shipments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.request.InboundShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentDetailResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentEventResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentResponseDto;
import com.banco.anteojos.backend.business.shipments.entities.Shipment;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentDirection;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentEvent;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentStatus;
import com.banco.anteojos.backend.business.shipments.exception.ShipmentNotFoundException;
import com.banco.anteojos.backend.business.shipments.exception.TrackingNumberAlreadyExistsException;
import com.banco.anteojos.backend.persistence.shipments.ShipmentEventPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.shipments.ShipmentPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.tracking.TrackingClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceHandler implements ShipmentService {

	private final ShipmentPostgresSqlRepository shipmentRepository;
	private final ShipmentEventPostgresSqlRepository eventRepository;
	private final TrackingWebhookHelper webhookHelper;
	private final TrackingClient trackingClient;

	@Override
	public ShipmentResponseDto createShipment(ShipmentCreationRequestDto request) {
		Shipment shipment = shipmentRepository.save(new Shipment(request.originBranch(),
				request.destinationBranch(), request.frameIds(), request.notes()));
		return toResponse(shipment);
	}

	@Override
	public ShipmentResponseDto createInboundShipment(Long donorId, InboundShipmentCreationRequestDto request) {
		String normalized = request.trackingNumber().trim();
		if (shipmentRepository.existsByTrackingNumber(normalized)) {
			throw new TrackingNumberAlreadyExistsException();
		}
		// Mismo orden que el despacho: si 17TRACK no acepta la guía, no queda una donación
		// "en camino" que nunca va a recibir novedades.
		trackingClient.register(normalized);
		return toResponse(shipmentRepository.save(Shipment.inbound(donorId, normalized, request.notes())));
	}

	@Override
	public ShipmentResponseDto getShipment(Long shipmentId) {
		return toResponse(findShipment(shipmentId));
	}

	@Override
	public List<ShipmentResponseDto> listShipments(ShipmentDirection direction) {
		return shipmentRepository.findByDirectionOrderByCreatedAtDesc(direction).stream().map(this::toResponse)
				.toList();
	}

	@Override
	public ShipmentResponseDto dispatch(Long shipmentId, String trackingNumber) {
		String normalized = trackingNumber.trim();
		if (shipmentRepository.existsByTrackingNumber(normalized)) {
			throw new TrackingNumberAlreadyExistsException();
		}
		Shipment shipment = findShipment(shipmentId);
		shipment.dispatch(normalized);
		// Registrar en 17TRACK antes de persistir: si falla, el envío sigue pendiente y el
		// operador reintenta; al revés quedaría "despachado" sin nadie que empuje actualizaciones.
		trackingClient.register(normalized);
		return toResponse(shipmentRepository.save(shipment));
	}

	@Override
	public ShipmentResponseDto cancel(Long shipmentId, String reason) {
		Shipment shipment = findShipment(shipmentId);
		shipment.cancel(reason);
		return toResponse(shipmentRepository.save(shipment));
	}

	@Override
	public ShipmentResponseDto receive(Long shipmentId, List<FrameResponseDto> frames) {
		Shipment shipment = findShipment(shipmentId);
		shipment.receive(frames.stream().map(FrameResponseDto::id).toList(),
				frames.stream().map(FrameResponseDto::donorId).toList());
		return toResponse(shipmentRepository.save(shipment));
	}

	@Override
	public void processTrackingPush(String sign, String rawBody) {
		webhookHelper.verifySignature(rawBody, sign);
		Optional<TrackingUpdate> parsed = webhookHelper.parse(rawBody);
		if (parsed.isEmpty()) {
			return;
		}
		TrackingUpdate update = parsed.get();

		// Número desconocido: 200 silencioso igual, si no 17TRACK reintenta un push que
		// nunca vamos a poder aplicar.
		Optional<Shipment> found = shipmentRepository.findByTrackingNumber(update.trackingNumber());
		if (found.isEmpty()) {
			log.info("Push de 17TRACK para un número que no seguimos: {}", update.trackingNumber());
			return;
		}
		Shipment shipment = found.get();

		Optional<ShipmentStatus> mapped = ShipmentStatus.fromTracking(update.rawStatus());
		if (mapped.isPresent()) {
			shipment.applyTrackingUpdate(mapped.get());
			shipmentRepository.save(shipment);
		} else {
			// El evento queda registrado igual: la trazabilidad no pierde lo que dijo el carrier.
			log.warn("Estado de 17TRACK sin mapear: '{}' para el envío {}", update.rawStatus(),
					shipment.getId());
		}
		eventRepository.save(new ShipmentEvent(shipment.getId(), update.rawStatus(),
				update.description(), update.location(), update.occurredAt()));
	}

	@Override
	public ShipmentDetailResponseDto buildDetail(ShipmentResponseDto shipment, List<FrameResponseDto> frames) {
		List<ShipmentEventResponseDto> events = eventRepository.findByShipmentIdOrderByIdAsc(shipment.id())
				.stream()
				.map(event -> new ShipmentEventResponseDto(event.getStatus(), event.getDescription(),
						event.getLocation(), event.getOccurredAt(), event.getReceivedAt()))
				.toList();
		return new ShipmentDetailResponseDto(shipment, frames, events);
	}

	@Override
	public long countDelivered(LocalDateTime from, LocalDateTime to) {
		// Solo salientes: una donación que llega no es un envío completado por la fundación.
		return shipmentRepository.countByDirectionAndStatusAndDeliveredAtBetween(ShipmentDirection.OUTBOUND,
				ShipmentStatus.DELIVERED, from, to);
	}

	private Shipment findShipment(Long shipmentId) {
		return shipmentRepository.findById(shipmentId).orElseThrow(ShipmentNotFoundException::new);
	}

	private ShipmentResponseDto toResponse(Shipment shipment) {
		return new ShipmentResponseDto(shipment.getId(), shipment.getDirection().name(), shipment.getDonorId(),
				shipment.getOriginBranch(), shipment.getDestinationBranch(), shipment.getTrackingNumber(),
				shipment.getStatus().name(), shipment.getNotes(), shipment.getCancellationReason(),
				shipment.getCreatedAt(), shipment.getDispatchedAt(), shipment.getDeliveredAt(),
				shipment.getReceivedAt(), List.copyOf(shipment.getFrameIds()));
	}
}
