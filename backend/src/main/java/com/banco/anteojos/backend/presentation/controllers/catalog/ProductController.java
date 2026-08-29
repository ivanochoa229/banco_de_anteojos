package com.banco.anteojos.backend.presentation.controllers.catalog;

import java.util.List;

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

import com.banco.anteojos.backend.business.catalog.dto.request.ProductCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductUpdateRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductResponseDto;
import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;
import com.banco.anteojos.backend.orchestrator.catalog.CatalogUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/catalog/products")
@RequiredArgsConstructor
public class ProductController {

	private final CatalogUseCaseOrchestrator catalogOrchestrator;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductResponseDto create(@Valid @RequestBody ProductCreationRequestDto request) {
		return catalogOrchestrator.createProduct(request);
	}

	/** Sin el parámetro devuelve el catálogo completo; con ?status=ACTIVE, lo disponible (RF-28). */
	@GetMapping
	public List<ProductResponseDto> list(@RequestParam(required = false) ProductStatus status) {
		return catalogOrchestrator.listProducts(status);
	}

	@GetMapping("/{productId}")
	public ProductResponseDto get(@PathVariable Long productId) {
		return catalogOrchestrator.getProduct(productId);
	}

	@PutMapping("/{productId}")
	public ProductResponseDto update(@PathVariable Long productId,
			@Valid @RequestBody ProductUpdateRequestDto request) {
		return catalogOrchestrator.updateProduct(productId, request);
	}

	/** Baja lógica (RF-30): sale del catálogo, sin vuelta atrás. */
	@PutMapping("/{productId}/discontinuation")
	public ProductResponseDto discontinue(@PathVariable Long productId) {
		return catalogOrchestrator.discontinueProduct(productId);
	}
}
