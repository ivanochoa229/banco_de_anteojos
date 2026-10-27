package com.banco.anteojos.backend.business.catalog.exception;

public class SaleNotFoundException extends RuntimeException {

	public SaleNotFoundException() {
		super("El anteojo todavía no se vendió");
	}
}
