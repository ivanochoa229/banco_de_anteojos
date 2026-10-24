package com.banco.anteojos.backend.business.applicants.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

// Sin email: para el beneficiario con login es su usuario de ingreso, y cambiarlo acá dejaría el
// registro del solicitante desfasado del login. El DNI tampoco: se fija al crear (RENAPER).
public record ApplicantProfileUpdateRequestDto(
		@NotBlank String firstName,
		@NotBlank String lastName,
		@Past LocalDate birthDate,
		String phone) {
}
