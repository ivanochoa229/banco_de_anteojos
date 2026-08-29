package com.banco.anteojos.backend.business.catalog;

import java.util.List;

import com.banco.anteojos.backend.business.catalog.dto.request.ProductCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductImageUploadRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductUpdateRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.SaleCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductImageResponseDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductResponseDto;
import com.banco.anteojos.backend.business.catalog.dto.response.SaleResponseDto;
import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;

public interface CatalogService {

	ProductResponseDto createProduct(ProductCreationRequestDto request);

	ProductResponseDto getProduct(Long productId);

	/** Con status en null devuelve el catálogo completo. */
	List<ProductResponseDto> listProducts(ProductStatus status);

	ProductResponseDto updateProduct(Long productId, ProductUpdateRequestDto request);

	ProductResponseDto discontinueProduct(Long productId);

	ProductResponseDto uploadProductImage(Long productId, ProductImageUploadRequestDto request);

	ProductImageResponseDto getProductImage(Long productId);

	/** Descuenta stock y registra la venta (RF-29) en una sola operación atómica del dominio. */
	SaleResponseDto sellProduct(Long productId, SaleCreationRequestDto request);

	List<SaleResponseDto> listSales(Long productId);
}
