package com.banco.anteojos.backend.useCase.catalog.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.banco.anteojos.backend.business.catalog.entities.Product;
import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;
import com.banco.anteojos.backend.business.catalog.exception.InsufficientStockException;
import com.banco.anteojos.backend.business.catalog.exception.InvalidProductTransitionException;

@Tag("unit")
class ProductTest {

	private Product product() {
		return new Product("  Ray-Ban Aviator  ", "  clásico  ", new BigDecimal("25000.00"), 10);
	}

	@Test
	void Create_Successful() {
		Product product = product();

		assertThat(product.getName()).isEqualTo("Ray-Ban Aviator");
		assertThat(product.getDescription()).isEqualTo("clásico");
		assertThat(product.getPrice()).isEqualByComparingTo("25000.00");
		assertThat(product.getStockQuantity()).isEqualTo(10);
		assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
		assertThat(product.getCreatedAt()).isNotNull();
	}

	@Test
	void Create_WithoutDescription() {
		Product product = new Product("Wayfarer", "  ", new BigDecimal("18000.00"), 5);

		assertThat(product.getDescription()).isNull();
	}

	@Test
	void Update_Successful() {
		Product product = product();

		product.update("  Ray-Ban Aviator Classic  ", "  edición limitada  ", new BigDecimal("27000.00"), 15);

		assertThat(product.getName()).isEqualTo("Ray-Ban Aviator Classic");
		assertThat(product.getDescription()).isEqualTo("edición limitada");
		assertThat(product.getPrice()).isEqualByComparingTo("27000.00");
		assertThat(product.getStockQuantity()).isEqualTo(15);
	}

	@Test
	void Sell_Successful() {
		Product product = product();

		product.sell(4);

		assertThat(product.getStockQuantity()).isEqualTo(6);
	}

	@Test
	void Sell_WhenInsufficientStock() {
		Product product = product();

		assertThatThrownBy(() -> product.sell(11))
				.isInstanceOf(InsufficientStockException.class)
				.hasMessageContaining("Stock insuficiente");
		// La venta rechazada no toca el stock.
		assertThat(product.getStockQuantity()).isEqualTo(10);
	}

	@Test
	void Sell_WhenDiscontinued() {
		Product product = product();
		product.discontinue();

		assertThatThrownBy(() -> product.sell(1))
				.isInstanceOf(InvalidProductTransitionException.class)
				.hasMessageContaining("de baja");
	}

	@Test
	void Discontinue_Successful() {
		Product product = product();

		product.discontinue();

		assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
	}

	@Test
	void Discontinue_WhenAlreadyDiscontinued() {
		Product product = product();
		product.discontinue();

		assertThatThrownBy(product::discontinue)
				.isInstanceOf(InvalidProductTransitionException.class)
				.hasMessageContaining("ya está de baja");
	}

	@Test
	void AttachImage_ReturnsPreviousKey() {
		Product product = product();

		assertThat(product.attachImage("products/1/a.png", "image/png", "a.png")).isNull();
		assertThat(product.hasImage()).isTrue();
		assertThat(product.attachImage("products/1/b.png", "image/png", "b.png"))
				.isEqualTo("products/1/a.png");
	}
}
