package com.banco.anteojos.backend.presentation.controllers.self;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentResponseDto;
import com.banco.anteojos.backend.business.security.CurrentUserService;
import com.banco.anteojos.backend.orchestrator.assignments.AssignmentUseCaseOrchestrator;

import lombok.RequiredArgsConstructor;

// Solo lectura: crear o mover una asignación es una decisión de matching que sigue siendo del
// operador. El applicantId nunca viene del cliente, sale del JWT vía CurrentUserService.
@RestController
@RequestMapping("/v1/me/assignments")
@RequiredArgsConstructor
public class SelfAssignmentController {

	private final AssignmentUseCaseOrchestrator assignmentOrchestrator;
	private final CurrentUserService currentUserService;

	@GetMapping
	public List<AssignmentResponseDto> list() {
		return assignmentOrchestrator.listAssignmentsByApplicant(currentUserService.getApplicantId());
	}
}
