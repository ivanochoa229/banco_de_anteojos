package com.banco.anteojos.backend.business.catalog.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/** Registro inmutable de una venta (RF-29): no tiene transiciones ni se edita. */
@Entity
@Table(name = "product_sales")
@Getter
public class Sale {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long productId;

	private Integer quantity;

	// Copia del precio al momento de vender: si el precio del producto cambia después, no debe
	// reescribir ventas ya cerradas.
	private BigDecimal unitPrice;

	private BigDecimal totalAmount;

	private String buyerName;

	private String notes;

	private LocalDateTime soldAt;

	protected Sale() {
	}

	public Sale(Long productId, Integer quantity, BigDecimal unitPrice, String buyerName, String notes) {
		this.productId = productId;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
		this.buyerName = buyerName == null || buyerName.isBlank() ? null : buyerName.trim();
		this.notes = notes == null || notes.isBlank() ? null : notes.trim();
		this.soldAt = LocalDateTime.now();
	}
}
