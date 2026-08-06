package com.banco.anteojos.backend.presentation.controllers.frames;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.banco.anteojos.backend.business.frames.dto.request.FrameImageUploadRequestDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameImageResponseDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.frames.exception.InvalidFrameImageException;
import com.banco.anteojos.backend.orchestrator.frames.FrameUseCaseOrchestrator;

import lombok.RequiredArgsConstructor;

// Sub-recurso singleton: un marco tiene a lo sumo una foto.
// PUT y no POST: subir otra la reemplaza.
@RestController
@RequestMapping("/v1/frames/{frameId}/image")
@RequiredArgsConstructor
public class FrameImageController {

	private final FrameUseCaseOrchestrator frameOrchestrator;

	@PutMapping
	public FrameResponseDto upload(@PathVariable Long frameId,
			@RequestParam("file") MultipartFile file) {
		return frameOrchestrator.uploadFrameImage(frameId, toUploadRequest(file));
	}

	/** Devuelve la presigned URL, no el binario: el backend no proxea archivos de R2. */
	@GetMapping
	public FrameImageResponseDto get(@PathVariable Long frameId) {
		return frameOrchestrator.getFrameImage(frameId);
	}

	private FrameImageUploadRequestDto toUploadRequest(MultipartFile file) {
		try {
			return new FrameImageUploadRequestDto(file.getBytes(), file.getContentType(),
					file.getOriginalFilename());
		} catch (IOException e) {
			throw new InvalidFrameImageException("No se pudo leer el archivo");
		}
	}
}
