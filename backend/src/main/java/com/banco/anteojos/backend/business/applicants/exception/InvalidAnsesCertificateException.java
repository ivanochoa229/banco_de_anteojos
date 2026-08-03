package com.banco.anteojos.backend.business.applicants.exception;

/**
 * La negativa de ANSES no pasó la validación automática (RF-04/05): no se pudo leer, el CUIL
 * no corresponde al solicitante o está vencida. No se persiste nada; el operador pide una
 * negativa nueva o recurre a la validación presencial (RF-06).
 */
public class InvalidAnsesCertificateException extends RuntimeException {

	public InvalidAnsesCertificateException(String message) {
		super(message);
	}
}
