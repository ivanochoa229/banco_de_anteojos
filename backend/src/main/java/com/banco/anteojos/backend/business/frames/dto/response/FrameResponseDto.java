package com.banco.anteojos.backend.business.frames.dto.response;

import java.time.LocalDateTime;

import com.banco.anteojos.backend.business.frames.entities.FrameMaterial;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;
import com.banco.anteojos.backend.business.frames.entities.FrameType;

public record FrameResponseDto(
		Long id,
		Long donorId,
		String sealCode,
		FrameType frameType,
		FrameMaterial material,
		Integer lensWidthMm,
		Integer bridgeWidthMm,
		Integer templeLengthMm,
		FrameStatus status,
		// null si el marco no tiene foto cargada; el probador virtual solo ofrece los que sí.
		String imageOriginalName,
		LocalDateTime receivedAt) {
}
