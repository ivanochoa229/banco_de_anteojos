package com.banco.anteojos.backend.business.catalog.exception;

public class InsufficientStockException extends RuntimeException {

	public InsufficientStockException(int requested, int available) {
		super("Stock insuficiente: se pidieron %d unidades y hay %d disponibles"
				.formatted(requested, available));
	}
}
