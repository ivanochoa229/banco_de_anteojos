package com.banco.anteojos.backend.business.catalog.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaleCreationRequestDto(
		@NotNull @Min(value = 1, message = "la cantidad tiene que ser al menos 1") Integer quantity,
		@Size(max = 150) String buyerName,
		@Size(max = 500) String notes) {
}
