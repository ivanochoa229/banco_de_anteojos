package com.banco.anteojos.backend.presentation.controllers.frames;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;
import com.banco.anteojos.backend.orchestrator.frames.FrameUseCaseOrchestrator;

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
}
