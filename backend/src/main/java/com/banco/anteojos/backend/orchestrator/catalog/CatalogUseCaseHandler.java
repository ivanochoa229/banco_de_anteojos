package com.banco.anteojos.backend.orchestrator.catalog;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.catalog.CatalogService;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductImageUploadRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductUpdateRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.SaleCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductImageResponseDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductResponseDto;
import com.banco.anteojos.backend.business.catalog.dto.response.SaleResponseDto;
import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;
import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;

import lombok.RequiredArgsConstructor;

/** Dominio autocontenido: sin datos que resolver en otro dominio, el orchestrator solo delega. */
@Component
@RequiredArgsConstructor
public class CatalogUseCaseHandler implements CatalogUseCaseOrchestrator {

	private final CatalogService catalogService;

	@Override
	public ProductResponseDto createProduct(ProductCreationRequestDto request) {
		return catalogService.createProduct(request);
	}

	@Override
	public ProductResponseDto getProduct(Long productId) {
		return catalogService.getProduct(productId);
	}

	@Override
	public PageResponseDto<ProductResponseDto> listProducts(ProductStatus status, Pageable pageable) {
		return catalogService.listProducts(status, pageable);
	}

	@Override
	public ProductResponseDto updateProduct(Long productId, ProductUpdateRequestDto request) {
		return catalogService.updateProduct(productId, request);
	}

	@Override
	public ProductResponseDto discontinueProduct(Long productId) {
		return catalogService.discontinueProduct(productId);
	}

	@Override
	public ProductResponseDto uploadProductImage(Long productId, ProductImageUploadRequestDto request) {
		return catalogService.uploadProductImage(productId, request);
	}

	@Override
	public ProductImageResponseDto getProductImage(Long productId) {
		return catalogService.getProductImage(productId);
	}

	// Transaccional por el lock del producto y porque descontar el stock y registrar la venta van
	// juntos: si falla el registro, el stock no puede quedar descontado.
	@Override
	@Transactional
	public SaleResponseDto sellProduct(Long productId, SaleCreationRequestDto request) {
		return catalogService.sellProduct(productId, request);
	}

	@Override
	public PageResponseDto<SaleResponseDto> listSales(Long productId, Pageable pageable) {
		return catalogService.listSales(productId, pageable);
	}
}
