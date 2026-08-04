package com.banco.anteojos.backend.presentation.controllers.assignments;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.assignments.dto.request.AssignmentCancellationRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentDetailResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentResponseDto;
import com.banco.anteojos.backend.orchestrator.assignments.AssignmentUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Cada paso del circuito es un sub-recurso en sustantivo y no un verbo en el path: la acción la
 * da el método HTTP. Son PUT porque marcar dos veces el mismo hito no debe duplicar nada.
 */
@RestController
@RequestMapping("/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

	private final AssignmentUseCaseOrchestrator assignmentOrchestrator;

	/** Con {@code ?live=true} devuelve solo las que están en curso: la cola de trabajo del día. */
	@GetMapping
	public List<AssignmentResponseDto> list(@RequestParam(defaultValue = "false") boolean live) {
		return assignmentOrchestrator.listAssignments(live);
	}

	/** Trazabilidad end-to-end del par de anteojos: donante, marco, beneficiario y cada hito (RF-16). */
	@GetMapping("/{assignmentId}")
	public AssignmentDetailResponseDto get(@PathVariable Long assignmentId) {
		return assignmentOrchestrator.getAssignment(assignmentId);
	}

	@PutMapping("/{assignmentId}/optician-dispatch")
	public AssignmentResponseDto sendToOptician(@PathVariable Long assignmentId) {
		return assignmentOrchestrator.sendToOptician(assignmentId);
	}

	@PutMapping("/{assignmentId}/optician-return")
	public AssignmentResponseDto returnFromOptician(@PathVariable Long assignmentId) {
		return assignmentOrchestrator.returnFromOptician(assignmentId);
	}

	@PutMapping("/{assignmentId}/delivery")
	public AssignmentResponseDto deliver(@PathVariable Long assignmentId) {
		return assignmentOrchestrator.deliver(assignmentId);
	}

	/** Cancelar devuelve el marco al inventario disponible. */
	@PutMapping("/{assignmentId}/cancellation")
	public AssignmentResponseDto cancel(@PathVariable Long assignmentId,
			@Valid @RequestBody AssignmentCancellationRequestDto request) {
		return assignmentOrchestrator.cancel(assignmentId, request.reason());
	}
}
