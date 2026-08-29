package com.banco.anteojos.backend.business.catalog.exception;

public class ProductImageNotFoundException extends RuntimeException {

	public ProductImageNotFoundException() {
		super("El producto no tiene una foto cargada");
	}
}
