package com.banco.anteojos.backend.presentation.controllers.donors;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.business.frames.dto.request.FrameCreationRequestDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;
import com.banco.anteojos.backend.orchestrator.frames.FrameUseCaseOrchestrator;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
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

	/** Con {@code ?status=AVAILABLE}, los que se pueden marcar como llegados en una donación. */
	@GetMapping
	public PageResponseDto<FrameResponseDto> list(@PathVariable Long donorId,
			@RequestParam(required = false) FrameStatus status,
			@RequestParam(defaultValue = "0") @PositiveOrZero int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return frameOrchestrator.listFramesByDonor(donorId, status, PageRequest.of(page, size));
	}
}
