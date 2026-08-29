package com.banco.anteojos.backend.business.catalog.exception;

public class ProductNotFoundException extends RuntimeException {

	public ProductNotFoundException() {
		super("El producto no existe");
	}
}
