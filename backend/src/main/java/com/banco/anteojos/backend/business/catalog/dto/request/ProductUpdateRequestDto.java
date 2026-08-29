package com.banco.anteojos.backend.business.catalog.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequestDto(
		@NotBlank @Size(max = 150) String name,
		@Size(max = 500) String description,
		@NotNull @DecimalMin(value = "0.0", message = "el precio no puede ser negativo") BigDecimal price,
		@NotNull @Min(value = 0, message = "el stock no puede ser negativo") Integer stockQuantity) {
}
