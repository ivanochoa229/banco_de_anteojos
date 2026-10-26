package com.banco.anteojos.backend.orchestrator.assignments;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.banco.anteojos.backend.business.assignments.dto.request.AssignmentCreationRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.request.DeliveryPhotoUploadRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentCreationResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentDetailResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentListItemResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.DeliveryPhotoResponseDto;
import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;

public interface AssignmentUseCaseOrchestrator {

	AssignmentCreationResponseDto createAssignment(Long applicantId, AssignmentCreationRequestDto request);

	AssignmentDetailResponseDto getAssignment(Long assignmentId);

	PageResponseDto<AssignmentListItemResponseDto> listAssignments(boolean onlyLive, Pageable pageable);

	List<AssignmentResponseDto> listAssignmentsByApplicant(Long applicantId);

	AssignmentResponseDto sendToOptician(Long assignmentId);

	AssignmentResponseDto returnFromOptician(Long assignmentId);

	AssignmentResponseDto deliver(Long assignmentId, DeliveryPhotoUploadRequestDto photo, boolean photoConsent);

	DeliveryPhotoResponseDto getDeliveryPhoto(Long assignmentId);

	AssignmentResponseDto cancel(Long assignmentId, String reason);
}
