package com.banco.anteojos.backend.business.assignments;

import java.util.List;

import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.request.AssignmentCreationRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentCreationResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentDetailResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentResponseDto;
import com.banco.anteojos.backend.business.donors.dto.response.DonorResponseDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;

public interface AssignmentService {

	/**
	 * @param eligibilityWarning motivo por el que el beneficiario no es elegible, o null si lo es.
	 *        Lo resuelve el orchestrator consultando al dominio applicants: acá solo se adjunta.
	 */
	AssignmentCreationResponseDto createAssignment(Long applicantId, AssignmentCreationRequestDto request,
			String eligibilityWarning);

	AssignmentResponseDto getAssignment(Long assignmentId);

	List<AssignmentResponseDto> listAssignments(boolean onlyLive);

	List<AssignmentResponseDto> listAssignmentsByApplicant(Long applicantId);

	AssignmentResponseDto sendToOptician(Long assignmentId);

	AssignmentResponseDto returnFromOptician(Long assignmentId);

	AssignmentResponseDto deliver(Long assignmentId);

	AssignmentResponseDto cancel(Long assignmentId, String reason);

	/** Arma la trazabilidad con los datos que el orchestrator ya resolvió en cada dominio. */
	AssignmentDetailResponseDto buildDetail(AssignmentResponseDto assignment, ApplicantResponseDto applicant,
			FrameResponseDto frame, DonorResponseDto donor);
}
