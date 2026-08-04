package com.banco.anteojos.backend.business.frames;

import java.util.List;

import com.banco.anteojos.backend.business.frames.dto.request.FrameCreationRequestDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;

public interface FrameService {

	FrameResponseDto createFrame(Long donorId, FrameCreationRequestDto request);

	FrameResponseDto getFrame(Long frameId);

	/** Con status en null devuelve el inventario completo. */
	List<FrameResponseDto> listFrames(FrameStatus status);

	List<FrameResponseDto> listFramesByDonor(Long donorId);

	// Transiciones del ciclo de vida. Las dispara el dominio assignments a través del
	// orchestrator: acá solo se valida el estado de origen y se mueve el marco.

	FrameResponseDto markAsAssigned(Long frameId);

	FrameResponseDto markAsAtOptician(Long frameId);

	FrameResponseDto markAsReady(Long frameId);

	FrameResponseDto markAsDelivered(Long frameId);

	FrameResponseDto returnToInventory(Long frameId);
}
