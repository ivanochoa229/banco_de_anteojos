package com.banco.anteojos.backend.presentation.controllers.shipments;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentCancellationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentDispatchRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentReceptionRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentDetailResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentListItemResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentResponseDto;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentDirection;
import com.banco.anteojos.backend.orchestrator.shipments.ShipmentUseCaseOrchestrator;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;

/** Como en assignments, cada acción es un sub-recurso en sustantivo y PUT. */
@RestController
@RequestMapping("/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

	private final ShipmentUseCaseOrchestrator shipmentOrchestrator;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ShipmentResponseDto create(@Valid @RequestBody ShipmentCreationRequestDto request) {
		return shipmentOrchestrator.createShipment(request);
	}

	/**
	 * OUTBOUND por defecto: la lista de envíos entre sucursales no se mezcla con las donaciones.
	 * Con {@code ?live=true}, solo lo que sigue en curso. Las donaciones traen el nombre del donante.
	 */
	@GetMapping
	public PageResponseDto<ShipmentListItemResponseDto> list(
			@RequestParam(defaultValue = "OUTBOUND") ShipmentDirection direction,
			@RequestParam(defaultValue = "false") boolean live,
			@RequestParam(defaultValue = "0") @PositiveOrZero int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return shipmentOrchestrator.listShipments(direction, live, PageRequest.of(page, size));
	}

	/** Trazabilidad del envío (RF-25): paquete, marcos y el historial que empujó el carrier. */
	@GetMapping("/{shipmentId}")
	public ShipmentDetailResponseDto get(@PathVariable Long shipmentId) {
		return shipmentOrchestrator.getShipment(shipmentId);
	}

	@PutMapping("/{shipmentId}/dispatch")
	public ShipmentResponseDto dispatch(@PathVariable Long shipmentId,
			@Valid @RequestBody ShipmentDispatchRequestDto request) {
		return shipmentOrchestrator.dispatch(shipmentId, request.trackingNumber());
	}

	/** Solo mientras el paquete está pendiente: despachado, ya está en manos de Vía Cargo. */
	@PutMapping("/{shipmentId}/cancellation")
	public ShipmentResponseDto cancel(@PathVariable Long shipmentId,
			@Valid @RequestBody ShipmentCancellationRequestDto request) {
		return shipmentOrchestrator.cancel(shipmentId, request.reason());
	}

	/** El operador abrió la donación entrante: los marcos ya están cargados bajo el donante. */
	@PutMapping("/{shipmentId}/reception")
	public ShipmentResponseDto receive(@PathVariable Long shipmentId,
			@Valid @RequestBody ShipmentReceptionRequestDto request) {
		return shipmentOrchestrator.receive(shipmentId, request);
	}
}
