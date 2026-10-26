package com.banco.anteojos.backend.orchestrator.catalog;

import org.springframework.data.domain.Pageable;

import com.banco.anteojos.backend.business.catalog.dto.request.ProductCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductImageUploadRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductUpdateRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.SaleCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductImageResponseDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductResponseDto;
import com.banco.anteojos.backend.business.catalog.dto.response.SaleResponseDto;
import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;
import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;

public interface CatalogUseCaseOrchestrator {

	ProductResponseDto createProduct(ProductCreationRequestDto request);

	ProductResponseDto getProduct(Long productId);

	PageResponseDto<ProductResponseDto> listProducts(ProductStatus status, Pageable pageable);

	ProductResponseDto updateProduct(Long productId, ProductUpdateRequestDto request);

	ProductResponseDto discontinueProduct(Long productId);

	ProductResponseDto uploadProductImage(Long productId, ProductImageUploadRequestDto request);

	ProductImageResponseDto getProductImage(Long productId);

	SaleResponseDto sellProduct(Long productId, SaleCreationRequestDto request);

	PageResponseDto<SaleResponseDto> listSales(Long productId, Pageable pageable);
}
