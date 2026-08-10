package com.banco.anteojos.backend.presentation.controllers.shipments;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.orchestrator.shipments.ShipmentUseCaseOrchestrator;

import lombok.RequiredArgsConstructor;

/**
 * Endpoint público (17TRACK no puede mandar un JWT): lo autentica la firma del header sign,
 * que se verifica sobre el cuerpo crudo — por eso el body se recibe como String y no como DTO,
 * cualquier re-serialización cambiaría los bytes firmados.
 */
@RestController
@RequestMapping("/v1/shipments/webhook")
@RequiredArgsConstructor
public class ShipmentWebhookController {

	private final ShipmentUseCaseOrchestrator shipmentOrchestrator;

	@PostMapping
	public void receive(@RequestHeader(value = "sign", required = false) String sign,
			@RequestBody String rawBody) {
		shipmentOrchestrator.processTrackingPush(sign, rawBody);
	}
}
