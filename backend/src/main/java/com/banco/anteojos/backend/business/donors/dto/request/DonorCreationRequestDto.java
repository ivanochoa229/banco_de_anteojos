package com.banco.anteojos.backend.business.donors.dto.request;

import com.banco.anteojos.backend.business.donors.entities.DonorType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DonorCreationRequestDto(
		@NotNull DonorType donorType,
		@NotBlank @Size(max = 150) String name,
		@Size(max = 20) String documentNumber,
		@Size(max = 30) String phone,
		@Email @Size(max = 255) String email) {
}
