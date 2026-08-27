package com.banco.anteojos.backend.useCase.catalog.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banco.anteojos.backend.business.catalog.CatalogServiceHandler;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductImageUploadRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.ProductUpdateRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.request.SaleCreationRequestDto;
import com.banco.anteojos.backend.business.catalog.dto.response.ProductResponseDto;
import com.banco.anteojos.backend.business.catalog.dto.response.SaleResponseDto;
import com.banco.anteojos.backend.business.catalog.entities.Product;
import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;
import com.banco.anteojos.backend.business.catalog.entities.Sale;
import com.banco.anteojos.backend.business.catalog.exception.InsufficientStockException;
import com.banco.anteojos.backend.business.catalog.exception.InvalidProductImageException;
import com.banco.anteojos.backend.business.catalog.exception.ProductImageNotFoundException;
import com.banco.anteojos.backend.business.catalog.exception.ProductNotFoundException;
import com.banco.anteojos.backend.persistence.catalog.ProductPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.catalog.SalePostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CatalogServiceHandlerTest {

	@Mock
	private ProductPostgresSqlRepository productRepository;

	@Mock
	private SalePostgresSqlRepository saleRepository;

	@Mock
	private R2StorageClient r2StorageClient;

	@InjectMocks
	private CatalogServiceHandler catalogServiceHandler;

	private Product product() {
		return new Product("Ray-Ban Aviator", "clásico", new BigDecimal("25000.00"), 10);
	}

	private void mockFind(Product product) {
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));
		when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	void CreateProduct_Successful() {
		when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

		ProductResponseDto response = catalogServiceHandler.createProduct(
				new ProductCreationRequestDto("Ray-Ban Aviator", "clásico", new BigDecimal("25000.00"), 10));

		assertThat(response.name()).isEqualTo("Ray-Ban Aviator");
		assertThat(response.stockQuantity()).isEqualTo(10);
		assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
	}

	@Test
	void GetProduct_WhenNotFound() {
		when(productRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> catalogServiceHandler.getProduct(999L))
				.isInstanceOf(ProductNotFoundException.class);
	}

	@Test
	void ListProducts_WithoutStatusReturnsWholeCatalog() {
		when(productRepository.findAllByOrderByNameAsc()).thenReturn(List.of(product()));

		List<ProductResponseDto> response = catalogServiceHandler.listProducts(null);

		assertThat(response).hasSize(1);
		verify(productRepository, never()).findByStatusOrderByNameAsc(any());
	}

	@Test
	void ListProducts_FilteredByStatus() {
		when(productRepository.findByStatusOrderByNameAsc(ProductStatus.ACTIVE))
				.thenReturn(List.of(product()));

		List<ProductResponseDto> response = catalogServiceHandler.listProducts(ProductStatus.ACTIVE);

		assertThat(response).hasSize(1);
		verify(productRepository, never()).findAllByOrderByNameAsc();
	}

	@Test
	void UpdateProduct_Successful() {
		mockFind(product());

		ProductResponseDto response = catalogServiceHandler.updateProduct(1L,
				new ProductUpdateRequestDto("Aviator Classic", "edición limitada", new BigDecimal("27000.00"), 15));

		assertThat(response.name()).isEqualTo("Aviator Classic");
		assertThat(response.stockQuantity()).isEqualTo(15);
	}

	@Test
	void DiscontinueProduct_Successful() {
		mockFind(product());

		ProductResponseDto response = catalogServiceHandler.discontinueProduct(1L);

		assertThat(response.status()).isEqualTo(ProductStatus.DISCONTINUED);
	}

	@Test
	void SellProduct_Successful() {
		mockFind(product());
		when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

		SaleResponseDto response = catalogServiceHandler.sellProduct(1L,
				new SaleCreationRequestDto(3, "Juana Pérez", "retira mañana"));

		assertThat(response.quantity()).isEqualTo(3);
		assertThat(response.unitPrice()).isEqualByComparingTo("25000.00");
		assertThat(response.totalAmount()).isEqualByComparingTo("75000.00");
		verify(productRepository).save(any(Product.class));
	}

	@Test
	void SellProduct_WhenInsufficientStock() {
		when(productRepository.findById(1L)).thenReturn(Optional.of(product()));

		assertThatThrownBy(() -> catalogServiceHandler.sellProduct(1L,
				new SaleCreationRequestDto(11, null, null)))
				.isInstanceOf(InsufficientStockException.class);
		verify(productRepository, never()).save(any());
		verify(saleRepository, never()).save(any());
	}

	@Test
	void ListSales_Successful() {
		when(saleRepository.findByProductIdOrderBySoldAtDesc(1L))
				.thenReturn(List.of(new Sale(1L, 1, new BigDecimal("25000.00"), null, null)));

		assertThat(catalogServiceHandler.listSales(1L)).hasSize(1);
	}

	// --- Foto del producto ---

	private ProductImageUploadRequestDto imageRequest(String contentType) {
		return new ProductImageUploadRequestDto(new byte[] { 1, 2, 3 }, contentType, "aviator.png");
	}

	@Test
	void UploadProductImage_Successful() {
		mockFind(product());

		ProductResponseDto response = catalogServiceHandler.uploadProductImage(1L, imageRequest("image/png"));

		assertThat(response.imageOriginalName()).isEqualTo("aviator.png");
		verify(r2StorageClient).upload(startsWith("products/1/"), any(), eq("image/png"));
	}

	@Test
	void UploadProductImage_WhenContentTypeNotAllowed() {
		when(productRepository.findById(1L)).thenReturn(Optional.of(product()));

		assertThatThrownBy(() -> catalogServiceHandler.uploadProductImage(1L, imageRequest("application/pdf")))
				.isInstanceOf(InvalidProductImageException.class)
				.hasMessageContaining("PNG, WEBP o JPEG");
		verify(r2StorageClient, never()).upload(any(), any(), any());
	}

	@Test
	void UploadProductImage_ReplacesPreviousImage() {
		Product product = product();
		product.attachImage("products/1/vieja.png", "image/png", "vieja.png");
		mockFind(product);

		catalogServiceHandler.uploadProductImage(1L, imageRequest("image/png"));

		verify(r2StorageClient).delete("products/1/vieja.png");
	}

	@Test
	void GetProductImage_Successful() {
		Product product = product();
		product.attachImage("products/1/foto.png", "image/png", "foto.png");
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));
		when(r2StorageClient.presignedGetUrl("products/1/foto.png"))
				.thenReturn(new PresignedUrl("https://r2/firmada", null));

		var response = catalogServiceHandler.getProductImage(1L);

		assertThat(response.url()).isEqualTo("https://r2/firmada");
		assertThat(response.fileName()).isEqualTo("foto.png");
	}

	@Test
	void GetProductImage_WhenProductHasNoImage() {
		when(productRepository.findById(1L)).thenReturn(Optional.of(product()));

		assertThatThrownBy(() -> catalogServiceHandler.getProductImage(1L))
				.isInstanceOf(ProductImageNotFoundException.class);
	}
}
