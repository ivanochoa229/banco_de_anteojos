package com.banco.anteojos.backend.presentation.controllers.frames;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

@RestController
@RequestMapping("/v1/frames")
@RequiredArgsConstructor
public class FrameController {

	private final FrameUseCaseOrchestrator frameOrchestrator;

	/**
	 * Sin filtros devuelve el inventario completo; con {@code ?status=AVAILABLE}, lo disponible
	 * (RF-13). {@code ?q=} busca por precinto y {@code ?withImage=true} deja solo los marcos con
	 * foto, que son los que se pueden probar en el probador virtual.
	 *
	 * No lleva el nombre del donante a propósito: el solicitante también lee este listado.
	 */
	@GetMapping
	public PageResponseDto<FrameResponseDto> list(@RequestParam(required = false) FrameStatus status,
			@RequestParam(defaultValue = "") String q,
			@RequestParam(defaultValue = "false") boolean withImage,
			@RequestParam(defaultValue = "0") @PositiveOrZero int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return frameOrchestrator.listFrames(status, q, withImage, PageRequest.of(page, size));
	}

	@GetMapping("/{frameId}")
	public FrameResponseDto get(@PathVariable Long frameId) {
		return frameOrchestrator.getFrame(frameId);
	}

	/** Corrige atributos cargados a mano; el precinto puede cambiar si hubo un error de tipeo. */
	@PutMapping("/{frameId}")
	public FrameResponseDto update(@PathVariable Long frameId, @Valid @RequestBody FrameCreationRequestDto request) {
		return frameOrchestrator.updateFrame(frameId, request);
	}

	// Sub-recurso en sustantivo, como el resto de las transiciones de estado del sistema
	// (/cancellation, /attendance): PUT porque repetir la baja no cambia nada.
	@PutMapping("/{frameId}/disposal")
	public FrameResponseDto discard(@PathVariable Long frameId) {
		return frameOrchestrator.discardFrame(frameId);
	}
}
