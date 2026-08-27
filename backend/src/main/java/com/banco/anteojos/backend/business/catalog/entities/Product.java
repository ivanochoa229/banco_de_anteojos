package com.banco.anteojos.backend.business.catalog.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banco.anteojos.backend.business.catalog.exception.InsufficientStockException;
import com.banco.anteojos.backend.business.catalog.exception.InvalidProductTransitionException;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/** Anteojo de sol a la venta (RF-28 a RF-30), sin relación con el inventario de marcos donados. */
@Entity
@Table(name = "products")
@Getter
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String description;

	private BigDecimal price;

	private Integer stockQuantity;

	@Enumerated(EnumType.STRING)
	private ProductStatus status;

	private String imageKey;

	private String imageContentType;

	private String imageOriginalName;

	private LocalDateTime createdAt;

	protected Product() {
	}

	public Product(String name, String description, BigDecimal price, Integer stockQuantity) {
		this.name = name.trim();
		this.description = description == null || description.isBlank() ? null : description.trim();
		this.price = price;
		this.stockQuantity = stockQuantity;
		this.status = ProductStatus.ACTIVE;
		this.createdAt = LocalDateTime.now();
	}

	/** Carga y actualización (RF-30) en un solo método: no amerita separar metadata de stock. */
	public void update(String name, String description, BigDecimal price, Integer stockQuantity) {
		this.name = name.trim();
		this.description = description == null || description.isBlank() ? null : description.trim();
		this.price = price;
		this.stockQuantity = stockQuantity;
	}

	/** Descuento automático de stock al vender (RF-29). */
	public void sell(int quantity) {
		if (status != ProductStatus.ACTIVE) {
			throw new InvalidProductTransitionException("El producto está de baja y no se puede vender");
		}
		if (quantity > stockQuantity) {
			throw new InsufficientStockException(quantity, stockQuantity);
		}
		this.stockQuantity -= quantity;
	}

	public void discontinue() {
		if (status == ProductStatus.DISCONTINUED) {
			throw new InvalidProductTransitionException("El producto ya está de baja");
		}
		this.status = ProductStatus.DISCONTINUED;
	}

	/** Devuelve la key anterior (null si no había), para que quien llama borre el objeto viejo de R2. */
	public String attachImage(String key, String contentType, String originalName) {
		String previousKey = this.imageKey;
		this.imageKey = key;
		this.imageContentType = contentType;
		this.imageOriginalName = originalName;
		return previousKey;
	}

	public boolean hasImage() {
		return imageKey != null;
	}
}
