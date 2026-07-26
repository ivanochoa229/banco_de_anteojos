package com.banco.anteojos.backend.business.donors.exception;

public class DonorNotFoundException extends RuntimeException {

	public DonorNotFoundException() {
		super("Donante no encontrado");
	}
}
