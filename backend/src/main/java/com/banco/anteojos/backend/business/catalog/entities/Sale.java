package com.banco.anteojos.backend.business.catalog.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/** Registro inmutable de la venta de un anteojo (RF-29): uno por producto, no se edita. */
@Entity
@Table(name = "product_sales")
@Getter
public class Sale {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long productId;

	// Copia del precio al momento de vender: la venta cerrada no depende del producto.
	private BigDecimal price;

	private String buyerName;

	private String notes;

	private LocalDateTime soldAt;

	protected Sale() {
	}

	public Sale(Long productId, BigDecimal price, String buyerName, String notes) {
		this.productId = productId;
		this.price = price;
		this.buyerName = buyerName == null || buyerName.isBlank() ? null : buyerName.trim();
		this.notes = notes == null || notes.isBlank() ? null : notes.trim();
		this.soldAt = LocalDateTime.now();
	}
}
