package com.banco.anteojos.backend.orchestrator.frames;

import java.util.List;

import org.springframework.stereotype.Component;

import com.banco.anteojos.backend.business.donors.DonorService;
import com.banco.anteojos.backend.business.frames.FrameService;
import com.banco.anteojos.backend.business.frames.dto.request.FrameCreationRequestDto;
import com.banco.anteojos.backend.business.frames.dto.request.FrameImageUploadRequestDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameImageResponseDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;

import lombok.RequiredArgsConstructor;

/**
 * Coordina frames con donors: un marco solo entra al inventario por una donación, así que la
 * existencia del donante se valida acá. FrameService nunca ve el repositorio de donors.
 */
@Component
@RequiredArgsConstructor
public class FrameUseCaseHandler implements FrameUseCaseOrchestrator {

	private final DonorService donorService;
	private final FrameService frameService;

	@Override
	public FrameResponseDto createFrame(Long donorId, FrameCreationRequestDto request) {
		donorService.getDonor(donorId);
		return frameService.createFrame(donorId, request);
	}

	@Override
	public FrameResponseDto getFrame(Long frameId) {
		return frameService.getFrame(frameId);
	}

	@Override
	public FrameResponseDto updateFrame(Long frameId, FrameCreationRequestDto request) {
		return frameService.updateFrame(frameId, request);
	}

	@Override
	public FrameResponseDto discardFrame(Long frameId) {
		return frameService.discardFrame(frameId);
	}

	// La foto es del marco y de nadie más: no hay nada cross-domain que coordinar acá.
	@Override
	public FrameResponseDto uploadFrameImage(Long frameId, FrameImageUploadRequestDto request) {
		return frameService.uploadFrameImage(frameId, request);
	}

	@Override
	public FrameImageResponseDto getFrameImage(Long frameId) {
		return frameService.getFrameImage(frameId);
	}

	@Override
	public List<FrameResponseDto> listFrames(FrameStatus status) {
		return frameService.listFrames(status);
	}

	@Override
	public List<FrameResponseDto> listFramesByDonor(Long donorId) {
		donorService.getDonor(donorId);
		return frameService.listFramesByDonor(donorId);
	}
}
