package com.banco.anteojos.backend.business.security.dto.request;

import java.time.LocalDate;

import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantCreationRequestDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ApplicantRegistrationRequestDto(
		@NotBlank String firstName,
		@NotBlank String lastName,
		@NotBlank @Pattern(regexp = "\\d{7,8}", message = "el DNI debe tener 7 u 8 dígitos") String dni,
		@Past LocalDate birthDate,
		String phone,
		@NotBlank @Email String email,
		@NotBlank @Size(min = 8, message = "la contraseña debe tener al menos 8 caracteres") String password) {

	public ApplicantCreationRequestDto toApplicantCreationRequest() {
		return new ApplicantCreationRequestDto(firstName, lastName, dni, birthDate, phone, email);
	}
}
