package com.banco.anteojos.backend.presentation.controllers.frames;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.frames.dto.request.FrameCreationRequestDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;
import com.banco.anteojos.backend.orchestrator.frames.FrameUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/frames")
@RequiredArgsConstructor
public class FrameController {

	private final FrameUseCaseOrchestrator frameOrchestrator;

	/** Sin el parámetro devuelve el inventario completo; con ?status=AVAILABLE, lo disponible (RF-13). */
	@GetMapping
	public List<FrameResponseDto> list(@RequestParam(required = false) FrameStatus status) {
		return frameOrchestrator.listFrames(status);
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
