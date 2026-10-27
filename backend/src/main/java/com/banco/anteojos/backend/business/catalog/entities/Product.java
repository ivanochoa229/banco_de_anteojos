package com.banco.anteojos.backend.business.catalog.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.banco.anteojos.backend.business.catalog.exception.InvalidProductTransitionException;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * Anteojo de sol a la venta (RF-28 a RF-30), sin relación con el inventario de marcos donados.
 * Cada producto es una unidad física única: no hay stock, se vende una sola vez.
 */
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

	@Enumerated(EnumType.STRING)
	private ProductStatus status;

	private String imageKey;

	private String imageContentType;

	private String imageOriginalName;

	private LocalDateTime createdAt;

	protected Product() {
	}

	public Product(String name, String description, BigDecimal price) {
		this.name = name.trim();
		this.description = description == null || description.isBlank() ? null : description.trim();
		this.price = price;
		this.status = ProductStatus.ACTIVE;
		this.createdAt = LocalDateTime.now();
	}

	/** Solo mientras está a la venta: un anteojo vendido tiene que seguir mostrando lo que se vendió. */
	public void update(String name, String description, BigDecimal price) {
		requireActive("editar");
		this.name = name.trim();
		this.description = description == null || description.isBlank() ? null : description.trim();
		this.price = price;
	}

	/** RF-29: el anteojo se vende una sola vez. */
	public void sell() {
		requireActive("vender");
		this.status = ProductStatus.SOLD;
	}

	public void discontinue() {
		requireActive("dar de baja");
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

	private void requireActive(String action) {
		if (status == ProductStatus.SOLD) {
			throw new InvalidProductTransitionException("El anteojo ya se vendió y no se puede " + action);
		}
		if (status == ProductStatus.DISCONTINUED) {
			throw new InvalidProductTransitionException("El anteojo está de baja y no se puede " + action);
		}
	}
}
