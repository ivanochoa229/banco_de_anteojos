package com.banco.anteojos.backend.business.catalog.dto.request;

import jakarta.validation.constraints.Size;

public record SaleCreationRequestDto(
		@Size(max = 150) String buyerName,
		@Size(max = 500) String notes) {
}
