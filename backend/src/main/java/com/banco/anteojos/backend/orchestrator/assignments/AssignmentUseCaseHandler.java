package com.banco.anteojos.backend.orchestrator.assignments;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.applicants.ApplicantService;
import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.EligibilityResponseDto;
import com.banco.anteojos.backend.business.assignments.AssignmentService;
import com.banco.anteojos.backend.business.assignments.dto.request.AssignmentCreationRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentCreationResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentDetailResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentResponseDto;
import com.banco.anteojos.backend.business.donors.DonorService;
import com.banco.anteojos.backend.business.frames.FrameService;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;

import lombok.RequiredArgsConstructor;

/**
 * Coordina los cuatro dominios que toca el circuito. Cada caso de uso que mueve la asignación
 * mueve también el marco, así que va en una transacción: si falla el segundo paso, el marco no
 * puede quedar reservado por una asignación que no se creó.
 */
@Component
@RequiredArgsConstructor
public class AssignmentUseCaseHandler implements AssignmentUseCaseOrchestrator {

	private final AssignmentService assignmentService;
	private final ApplicantService applicantService;
	private final FrameService frameService;
	private final DonorService donorService;

	@Override
	@Transactional
	public AssignmentCreationResponseDto createAssignment(Long applicantId, AssignmentCreationRequestDto request) {
		// Pertenencia primero: que la receta sea de este solicitante y no de otro.
		applicantService.getPrescription(applicantId, request.prescriptionId());
		// Reservar el marco antes de crear la asignación: es el paso que puede fallar por
		// estado (ya asignado) y así no queda una asignación colgada.
		frameService.markAsAssigned(request.frameId());
		EligibilityResponseDto eligibility = applicantService.checkEligibility(applicantId);
		return assignmentService.createAssignment(applicantId, request, eligibility.warning());
	}

	@Override
	public AssignmentDetailResponseDto getAssignment(Long assignmentId) {
		AssignmentResponseDto assignment = assignmentService.getAssignment(assignmentId);
		ApplicantResponseDto applicant = applicantService.getApplicant(assignment.applicantId());
		FrameResponseDto frame = frameService.getFrame(assignment.frameId());
		return assignmentService.buildDetail(assignment, applicant, frame,
				donorService.getDonor(frame.donorId()));
	}

	@Override
	public List<AssignmentResponseDto> listAssignments(boolean onlyLive) {
		return assignmentService.listAssignments(onlyLive);
	}

	@Override
	public List<AssignmentResponseDto> listAssignmentsByApplicant(Long applicantId) {
		applicantService.getApplicant(applicantId);
		return assignmentService.listAssignmentsByApplicant(applicantId);
	}

	@Override
	@Transactional
	public AssignmentResponseDto sendToOptician(Long assignmentId) {
		AssignmentResponseDto assignment = assignmentService.sendToOptician(assignmentId);
		frameService.markAsAtOptician(assignment.frameId());
		return assignment;
	}

	@Override
	@Transactional
	public AssignmentResponseDto returnFromOptician(Long assignmentId) {
		AssignmentResponseDto assignment = assignmentService.returnFromOptician(assignmentId);
		frameService.markAsReady(assignment.frameId());
		return assignment;
	}

	@Override
	@Transactional
	public AssignmentResponseDto deliver(Long assignmentId) {
		AssignmentResponseDto assignment = assignmentService.deliver(assignmentId);
		frameService.markAsDelivered(assignment.frameId());
		return assignment;
	}

	@Override
	@Transactional
	public AssignmentResponseDto cancel(Long assignmentId, String reason) {
		AssignmentResponseDto assignment = assignmentService.cancel(assignmentId, reason);
		frameService.returnToInventory(assignment.frameId());
		return assignment;
	}
}
