package com.banco.anteojos.backend.business.catalog.entities;

public enum ProductStatus {

	ACTIVE,
	/** Baja lógica (RF-30): sale del catálogo y no se puede vender más. No hay vuelta atrás. */
	DISCONTINUED
}
