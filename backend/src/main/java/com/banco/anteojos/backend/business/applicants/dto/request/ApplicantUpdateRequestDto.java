package com.banco.anteojos.backend.business.applicants.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

public record ApplicantUpdateRequestDto(
		@NotBlank String firstName,
		@NotBlank String lastName,
		@Past LocalDate birthDate,
		String phone,
		@Email String email) {
}
