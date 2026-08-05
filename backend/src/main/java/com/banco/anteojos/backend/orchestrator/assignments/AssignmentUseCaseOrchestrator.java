package com.banco.anteojos.backend.orchestrator.assignments;

import java.util.List;

import com.banco.anteojos.backend.business.assignments.dto.request.AssignmentCreationRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentCreationResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentDetailResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentResponseDto;

public interface AssignmentUseCaseOrchestrator {

	AssignmentCreationResponseDto createAssignment(Long applicantId, AssignmentCreationRequestDto request);

	AssignmentDetailResponseDto getAssignment(Long assignmentId);

	List<AssignmentResponseDto> listAssignments(boolean onlyLive);

	List<AssignmentResponseDto> listAssignmentsByApplicant(Long applicantId);

	AssignmentResponseDto sendToOptician(Long assignmentId);

	AssignmentResponseDto returnFromOptician(Long assignmentId);

	AssignmentResponseDto deliver(Long assignmentId);

	AssignmentResponseDto cancel(Long assignmentId, String reason);
}
