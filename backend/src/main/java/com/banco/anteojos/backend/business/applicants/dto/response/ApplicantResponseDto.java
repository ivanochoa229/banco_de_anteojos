package com.banco.anteojos.backend.business.applicants.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ApplicantResponseDto(
		Long id,
		String firstName,
		String lastName,
		String dni,
		String cuil,
		LocalDate birthDate,
		String phone,
		String email,
		boolean identityValidated,
		LocalDateTime createdAt) {
}
