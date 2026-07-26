package com.banco.anteojos.backend.business.frames.dto.request;

import com.banco.anteojos.backend.business.frames.entities.FrameMaterial;
import com.banco.anteojos.backend.business.frames.entities.FrameType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Las medidas son opcionales (no todo marco donado viene grabado) pero acotadas a rangos
// físicamente posibles, para que un tipeo tipo 500 no entre al inventario.
public record FrameCreationRequestDto(
		@NotBlank @Size(max = 30) String sealCode,
		@NotNull FrameType frameType,
		@NotNull FrameMaterial material,
		@Min(20) @Max(80) Integer lensWidthMm,
		@Min(10) @Max(30) Integer bridgeWidthMm,
		@Min(100) @Max(160) Integer templeLengthMm) {
}
