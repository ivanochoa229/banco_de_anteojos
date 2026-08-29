package com.banco.anteojos.backend.business.catalog;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.catalog.dto.request.ProductCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductImageUploadRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductUpdateRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.SaleCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductImageResponseDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductResponseDto;
import com.banco.anteojos.backend.business.catalog.dto.response.SaleResponseDto;
import com.banco.anteojos.backend.business.catalog.entities.Product;
import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;
import com.banco.anteojos.backend.business.catalog.entities.Sale;
import com.banco.anteojos.backend.business.catalog.exception.InvalidProductImageException;
import com.banco.anteojos.backend.business.catalog.exception.ProductImageNotFoundException;
import com.banco.anteojos.backend.business.catalog.exception.ProductNotFoundException;
import com.banco.anteojos.backend.persistence.catalog.ProductPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.catalog.SalePostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogServiceHandler implements CatalogService {

	/**
	 * A diferencia de las fotos de marcos, acá no hace falta canal alfa (no se superponen sobre
	 * una cara en el probador): JPEG entra sin problema.
	 */
	private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
			"image/png", "png",
			"image/webp", "webp",
			"image/jpeg", "jpg");

	private final ProductPostgresSqlRepository productRepository;
	private final SalePostgresSqlRepository saleRepository;
	private final R2StorageClient r2StorageClient;

	@Override
	public ProductResponseDto createProduct(ProductCreationRequestDto request) {
		Product product = productRepository.save(new Product(request.name(), request.description(),
				request.price(), request.stockQuantity()));
		return toResponse(product);
	}

	@Override
	public ProductResponseDto getProduct(Long productId) {
		return toResponse(findProduct(productId));
	}

	@Override
	public List<ProductResponseDto> listProducts(ProductStatus status) {
		List<Product> products = status == null
				? productRepository.findAllByOrderByNameAsc()
				: productRepository.findByStatusOrderByNameAsc(status);
		return products.stream().map(this::toResponse).toList();
	}

	@Override
	public ProductResponseDto updateProduct(Long productId, ProductUpdateRequestDto request) {
		Product product = findProduct(productId);
		product.update(request.name(), request.description(), request.price(), request.stockQuantity());
		return toResponse(productRepository.save(product));
	}

	@Override
	public ProductResponseDto discontinueProduct(Long productId) {
		Product product = findProduct(productId);
		product.discontinue();
		return toResponse(productRepository.save(product));
	}

	@Override
	public ProductResponseDto uploadProductImage(Long productId, ProductImageUploadRequestDto request) {
		Product product = findProduct(productId);
		String extension = validateImage(request);

		String key = "products/%d/%s.%s".formatted(productId, UUID.randomUUID(), extension);
		r2StorageClient.upload(key, request.content(), request.contentType());

		String previousKey = product.attachImage(key, request.contentType(), request.originalName());
		ProductResponseDto response = toResponse(productRepository.save(product));

		// Limpiar la foto reemplazada no es crítico: si falla queda un huérfano en R2, pero el
		// producto ya apunta a la nueva.
		if (previousKey != null) {
			try {
				r2StorageClient.delete(previousKey);
			} catch (RuntimeException e) {
				log.warn("No se pudo borrar la foto reemplazada {} del producto {}", previousKey, productId, e);
			}
		}
		return response;
	}

	@Override
	public ProductImageResponseDto getProductImage(Long productId) {
		Product product = findProduct(productId);
		if (!product.hasImage()) {
			throw new ProductImageNotFoundException();
		}
		PresignedUrl presigned = r2StorageClient.presignedGetUrl(product.getImageKey());
		return new ProductImageResponseDto(presigned.url(), presigned.expiresAt(),
				product.getImageOriginalName(), product.getImageContentType());
	}

	@Override
	public SaleResponseDto sellProduct(Long productId, SaleCreationRequestDto request) {
		Product product = findProduct(productId);
		product.sell(request.quantity());
		productRepository.save(product);

		Sale sale = saleRepository.save(new Sale(productId, request.quantity(), product.getPrice(),
				request.buyerName(), request.notes()));
		return toResponse(sale);
	}

	@Override
	public List<SaleResponseDto> listSales(Long productId) {
		return saleRepository.findByProductIdOrderBySoldAtDesc(productId).stream()
				.map(this::toResponse).toList();
	}

	/** Devuelve la extensión que corresponde al content type, ya validado. */
	private String validateImage(ProductImageUploadRequestDto request) {
		if (request.content() == null || request.content().length == 0) {
			throw new InvalidProductImageException("El archivo está vacío");
		}
		String extension = ALLOWED_IMAGE_TYPES.get(request.contentType());
		if (extension == null) {
			throw new InvalidProductImageException("La foto del producto debe ser PNG, WEBP o JPEG");
		}
		return extension;
	}

	private Product findProduct(Long productId) {
		return productRepository.findById(productId).orElseThrow(ProductNotFoundException::new);
	}

	private ProductResponseDto toResponse(Product product) {
		return new ProductResponseDto(product.getId(), product.getName(), product.getDescription(),
				product.getPrice(), product.getStockQuantity(), product.getStatus(),
				product.getImageOriginalName(), product.getCreatedAt());
	}

	private SaleResponseDto toResponse(Sale sale) {
		return new SaleResponseDto(sale.getId(), sale.getProductId(), sale.getQuantity(),
				sale.getUnitPrice(), sale.getTotalAmount(), sale.getBuyerName(), sale.getNotes(),
				sale.getSoldAt());
	}
}
