package com.banco.anteojos.backend.presentation.controllers.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.catalog.dto.request.SaleCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.response.SaleResponseDto;
import com.banco.anteojos.backend.orchestrator.catalog.CatalogUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Sub-recurso singleton, como la foto: cada anteojo es único y se vende a lo sumo una vez.
@RestController
@RequestMapping("/v1/catalog/products/{productId}/sale")
@RequiredArgsConstructor
public class ProductSaleController {

	private final CatalogUseCaseOrchestrator catalogOrchestrator;

	/** Registra la venta y marca el anteojo como vendido en la misma operación (RF-29). */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SaleResponseDto sell(@PathVariable Long productId, @Valid @RequestBody SaleCreationRequestDto request) {
		return catalogOrchestrator.sellProduct(productId, request);
	}

	@GetMapping
	public SaleResponseDto get(@PathVariable Long productId) {
		return catalogOrchestrator.getSale(productId);
	}
}
