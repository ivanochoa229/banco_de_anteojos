package com.banco.anteojos.backend.orchestrator.frames;

import org.springframework.data.domain.Pageable;

import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.business.frames.dto.request.FrameCreationRequestDto;
import com.banco.anteojos.backend.business.frames.dto.request.FrameImageUploadRequestDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameImageResponseDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;

public interface FrameUseCaseOrchestrator {

	FrameResponseDto createFrame(Long donorId, FrameCreationRequestDto request);

	FrameResponseDto getFrame(Long frameId);

	FrameResponseDto updateFrame(Long frameId, FrameCreationRequestDto request);

	FrameResponseDto discardFrame(Long frameId);

	FrameResponseDto uploadFrameImage(Long frameId, FrameImageUploadRequestDto request);

	FrameImageResponseDto getFrameImage(Long frameId);

	PageResponseDto<FrameResponseDto> listFrames(FrameStatus status, String search, boolean withImage,
			Pageable pageable);

	PageResponseDto<FrameResponseDto> listFramesByDonor(Long donorId, FrameStatus status, Pageable pageable);
}
