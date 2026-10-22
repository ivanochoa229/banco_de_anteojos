package com.banco.anteojos.backend.presentation.controllers.donors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.shipments.dto.request.InboundShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentResponseDto;
import com.banco.anteojos.backend.orchestrator.shipments.ShipmentUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Donación que un donante despacha por Vía Cargo hacia la fundación. Cuelga del donante, como
 * DonorFrameController; el resto del ciclo (detalle, recepción) sigue en /v1/shipments/{id}.
 */
@RestController
@RequestMapping("/v1/donors/{donorId}/shipments")
@RequiredArgsConstructor
public class DonorShipmentController {

	private final ShipmentUseCaseOrchestrator shipmentOrchestrator;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ShipmentResponseDto create(@PathVariable Long donorId,
			@Valid @RequestBody InboundShipmentCreationRequestDto request) {
		return shipmentOrchestrator.createInboundShipment(donorId, request);
	}
}
