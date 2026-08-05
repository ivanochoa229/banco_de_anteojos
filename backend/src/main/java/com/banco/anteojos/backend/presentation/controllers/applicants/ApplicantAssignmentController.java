package com.banco.anteojos.backend.presentation.controllers.applicants;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.assignments.dto.request.AssignmentCreationRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentCreationResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentResponseDto;
import com.banco.anteojos.backend.orchestrator.assignments.AssignmentUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// La asignación se crea colgando del beneficiario, que es el dueño del recurso.
@RestController
@RequestMapping("/v1/applicants/{applicantId}/assignments")
@RequiredArgsConstructor
public class ApplicantAssignmentController {

	private final AssignmentUseCaseOrchestrator assignmentOrchestrator;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AssignmentCreationResponseDto create(@PathVariable Long applicantId,
			@Valid @RequestBody AssignmentCreationRequestDto request) {
		return assignmentOrchestrator.createAssignment(applicantId, request);
	}

	@GetMapping
	public List<AssignmentResponseDto> list(@PathVariable Long applicantId) {
		return assignmentOrchestrator.listAssignmentsByApplicant(applicantId);
	}
}
