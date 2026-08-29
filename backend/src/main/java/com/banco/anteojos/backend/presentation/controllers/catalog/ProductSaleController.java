package com.banco.anteojos.backend.presentation.controllers.catalog;

import java.util.List;

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

@RestController
@RequestMapping("/v1/catalog/products/{productId}/sales")
@RequiredArgsConstructor
public class ProductSaleController {

	private final CatalogUseCaseOrchestrator catalogOrchestrator;

	/** Registra la venta y descuenta el stock en la misma operación (RF-29). */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SaleResponseDto sell(@PathVariable Long productId, @Valid @RequestBody SaleCreationRequestDto request) {
		return catalogOrchestrator.sellProduct(productId, request);
	}

	@GetMapping
	public List<SaleResponseDto> list(@PathVariable Long productId) {
		return catalogOrchestrator.listSales(productId);
	}
}
