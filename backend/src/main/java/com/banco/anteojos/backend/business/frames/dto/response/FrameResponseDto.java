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
		LocalDateTime receivedAt) {
}
