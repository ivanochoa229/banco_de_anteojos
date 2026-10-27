package com.banco.anteojos.backend.business.catalog.entities;

/** Cada producto es un anteojo único: sale de ACTIVE una sola vez, a SOLD o a DISCONTINUED. */
public enum ProductStatus {

	/** A la venta. */
	ACTIVE,
	/** Vendido (RF-29): tiene su Sale y ya no se edita ni se vende. */
	SOLD,
	/** Baja lógica (RF-30): sale del catálogo y no se puede vender más. No hay vuelta atrás. */
	DISCONTINUED
}
