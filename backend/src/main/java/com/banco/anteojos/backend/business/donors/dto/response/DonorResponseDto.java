package com.banco.anteojos.backend.business.donors.dto.response;

import java.time.LocalDateTime;

import com.banco.anteojos.backend.business.donors.entities.DonorType;

public record DonorResponseDto(
		Long id,
		DonorType donorType,
		String name,
		String documentNumber,
		String phone,
		String email,
		LocalDateTime createdAt) {
}
