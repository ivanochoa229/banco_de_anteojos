package com.banco.anteojos.backend.business.applicants;

import java.time.LocalDate;

/** Datos crudos extraídos del PDF de la negativa, todavía sin validar contra el solicitante. */
public record ParsedAnsesCertificate(
		String cuil,
		String transactionNumber,
		LocalDate issueDate) {
}
