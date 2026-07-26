package com.banco.anteojos.backend.business.applicants.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

public record ApplicantCreationRequestDto(
		@NotBlank String firstName,
		@NotBlank String lastName,
		@NotBlank @Pattern(regexp = "\\d{7,8}", message = "el DNI debe tener 7 u 8 dígitos") String dni,
		@Past LocalDate birthDate,
		String phone,
		@Email String email) {
}
