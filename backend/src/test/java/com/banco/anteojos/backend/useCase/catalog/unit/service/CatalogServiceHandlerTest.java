package com.banco.anteojos.backend.useCase.catalog.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
import com.banco.anteojos.backend.business.catalog.exception.InvalidProductTransitionException;
import com.banco.anteojos.backend.business.catalog.exception.InvalidProductImageException;
import com.banco.anteojos.backend.business.catalog.exception.ProductImageNotFoundException;
import com.banco.anteojos.backend.business.catalog.exception.ProductNotFoundException;
import com.banco.anteojos.backend.business.catalog.exception.SaleNotFoundException;
import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
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

	private static final Pageable PAGE = PageRequest.of(0, 20);

	private Product product() {
		return new Product("Ray-Ban Aviator", "clásico", new BigDecimal("25000.00"));
	}

	private void mockFind(Product product) {
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));
		when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	void CreateProduct_Successful() {
		when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

		ProductResponseDto response = catalogServiceHandler.createProduct(
				new ProductCreationRequestDto("Ray-Ban Aviator", "clásico", new BigDecimal("25000.00")));

		assertThat(response.name()).isEqualTo("Ray-Ban Aviator");
		assertThat(response.status()).isEqualTo(ProductStatus.ACTIVE);
		assertThat(response.imageUrl()).isNull();
	}

	@Test
	void GetProduct_WhenNotFound() {
		when(productRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> catalogServiceHandler.getProduct(999L))
				.isInstanceOf(ProductNotFoundException.class);
	}

	@Test
	void ListProducts_WithoutStatusReturnsWholeCatalog() {
		when(productRepository.findAllByOrderByNameAscIdAsc(PAGE)).thenReturn(new PageImpl<>(List.of(product()), PAGE, 1));

		PageResponseDto<ProductResponseDto> response = catalogServiceHandler.listProducts(null, PAGE);

		assertThat(response.content()).hasSize(1);
		verify(productRepository, never()).findByStatusOrderByNameAscIdAsc(any(), any());
	}

	@Test
	void ListProducts_FilteredByStatus() {
		when(productRepository.findByStatusOrderByNameAscIdAsc(ProductStatus.ACTIVE, PAGE))
				.thenReturn(new PageImpl<>(List.of(product()), PAGE, 1));

		PageResponseDto<ProductResponseDto> response = catalogServiceHandler.listProducts(ProductStatus.ACTIVE, PAGE);

		assertThat(response.content()).hasSize(1);
		verify(productRepository, never()).findAllByOrderByNameAscIdAsc(any());
	}

	@Test
	void ListProducts_IncludesImageUrl() {
		Product product = product();
		product.attachImage("products/1/foto.png", "image/png", "foto.png");
		when(productRepository.findAllByOrderByNameAscIdAsc(PAGE)).thenReturn(new PageImpl<>(List.of(product), PAGE, 1));
		when(r2StorageClient.presignedGetUrl("products/1/foto.png"))
				.thenReturn(new PresignedUrl("https://r2/firmada", null));

		PageResponseDto<ProductResponseDto> response = catalogServiceHandler.listProducts(null, PAGE);

		assertThat(response.content().get(0).imageUrl()).isEqualTo("https://r2/firmada");
	}

	@Test
	void UpdateProduct_Successful() {
		mockFind(product());

		ProductResponseDto response = catalogServiceHandler.updateProduct(1L,
				new ProductUpdateRequestDto("Aviator Classic", "edición limitada", new BigDecimal("27000.00")));

		assertThat(response.name()).isEqualTo("Aviator Classic");
		assertThat(response.price()).isEqualByComparingTo("27000.00");
	}

	@Test
	void DiscontinueProduct_Successful() {
		mockFind(product());

		ProductResponseDto response = catalogServiceHandler.discontinueProduct(1L);

		assertThat(response.status()).isEqualTo(ProductStatus.DISCONTINUED);
	}

	@Test
	void SellProduct_Successful() {
		when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product()));
		when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
		when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

		SaleResponseDto response = catalogServiceHandler.sellProduct(1L,
				new SaleCreationRequestDto("Juana Pérez", "retira mañana"));

		assertThat(response.price()).isEqualByComparingTo("25000.00");
		assertThat(response.buyerName()).isEqualTo("Juana Pérez");
		verify(productRepository).save(argThat(product -> product.getStatus() == ProductStatus.SOLD));
	}

	@Test
	void SellProduct_WhenAlreadySold() {
		Product product = product();
		product.sell();
		when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

		assertThatThrownBy(() -> catalogServiceHandler.sellProduct(1L, new SaleCreationRequestDto(null, null)))
				.isInstanceOf(InvalidProductTransitionException.class);
		verify(productRepository, never()).save(any());
		verify(saleRepository, never()).save(any());
	}

	@Test
	void GetSale_Successful() {
		when(productRepository.findById(1L)).thenReturn(Optional.of(product()));
		when(saleRepository.findByProductId(1L))
				.thenReturn(Optional.of(new Sale(1L, new BigDecimal("25000.00"), "Juana Pérez", null)));

		assertThat(catalogServiceHandler.getSale(1L).buyerName()).isEqualTo("Juana Pérez");
	}

	@Test
	void GetSale_WhenNotSold() {
		when(productRepository.findById(1L)).thenReturn(Optional.of(product()));
		when(saleRepository.findByProductId(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> catalogServiceHandler.getSale(1L))
				.isInstanceOf(SaleNotFoundException.class);
	}

	// --- Foto del producto ---

	private ProductImageUploadRequestDto imageRequest(String contentType) {
		return new ProductImageUploadRequestDto(new byte[] { 1, 2, 3 }, contentType, "aviator.png");
	}

	@Test
	void UploadProductImage_Successful() {
		mockFind(product());
		when(r2StorageClient.presignedGetUrl(startsWith("products/1/")))
				.thenReturn(new PresignedUrl("https://r2/firmada", null));

		ProductResponseDto response = catalogServiceHandler.uploadProductImage(1L, imageRequest("image/png"));

		assertThat(response.imageOriginalName()).isEqualTo("aviator.png");
		assertThat(response.imageUrl()).isEqualTo("https://r2/firmada");
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
		when(r2StorageClient.presignedGetUrl(startsWith("products/1/")))
				.thenReturn(new PresignedUrl("https://r2/firmada", null));

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
