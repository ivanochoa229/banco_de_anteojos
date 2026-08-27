package com.banco.anteojos.backend.orchestrator.catalog;

import java.util.List;

import org.springframework.stereotype.Component;

import com.banco.anteojos.backend.business.catalog.CatalogService;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductImageUploadRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductUpdateRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.SaleCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductImageResponseDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductResponseDto;
import com.banco.anteojos.backend.business.catalog.dto.response.SaleResponseDto;
import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;

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
	public List<ProductResponseDto> listProducts(ProductStatus status) {
		return catalogService.listProducts(status);
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

	@Override
	public SaleResponseDto sellProduct(Long productId, SaleCreationRequestDto request) {
		return catalogService.sellProduct(productId, request);
	}

	@Override
	public List<SaleResponseDto> listSales(Long productId) {
		return catalogService.listSales(productId);
	}
}
