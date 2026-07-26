package com.banco.anteojos.backend.presentation.controllers.donors;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.frames.dto.request.FrameCreationRequestDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.orchestrator.frames.FrameUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Los marcos entran al inventario por una donación, de ahí la ruta jerárquica bajo el donante. */
@RestController
@RequestMapping("/v1/donors/{donorId}/frames")
@RequiredArgsConstructor
public class DonorFrameController {

	private final FrameUseCaseOrchestrator frameOrchestrator;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FrameResponseDto create(@PathVariable Long donorId,
			@Valid @RequestBody FrameCreationRequestDto request) {
		return frameOrchestrator.createFrame(donorId, request);
	}

	@GetMapping
	public List<FrameResponseDto> list(@PathVariable Long donorId) {
		return frameOrchestrator.listFramesByDonor(donorId);
	}
}
