package com.banco.anteojos.backend.business.assignments;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.request.AssignmentCreationRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentCreationResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentDetailResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.MonthlyDeliveryResponseDto;
import com.banco.anteojos.backend.business.assignments.entities.Assignment;
import com.banco.anteojos.backend.business.assignments.exception.AssignmentNotFoundException;
import com.banco.anteojos.backend.business.donors.dto.response.DonorResponseDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.persistence.assignments.AssignmentPostgresSqlRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssignmentServiceHandler implements AssignmentService {

	private final AssignmentPostgresSqlRepository assignmentRepository;

	@Override
	public AssignmentCreationResponseDto createAssignment(Long applicantId,
			AssignmentCreationRequestDto request, String eligibilityWarning) {
		Assignment assignment = assignmentRepository.save(new Assignment(applicantId, request.frameId(),
				request.prescriptionId(), request.notes()));
		return new AssignmentCreationResponseDto(toResponse(assignment), eligibilityWarning);
	}

	@Override
	public AssignmentResponseDto getAssignment(Long assignmentId) {
		return toResponse(findAssignment(assignmentId));
	}

	@Override
	public List<AssignmentResponseDto> listAssignments(boolean onlyLive) {
		List<Assignment> assignments = onlyLive
				? assignmentRepository.findLive()
				: assignmentRepository.findAllByOrderByAssignedAtDesc();
		return assignments.stream().map(this::toResponse).toList();
	}

	@Override
	public List<AssignmentResponseDto> listAssignmentsByApplicant(Long applicantId) {
		return assignmentRepository.findByApplicantIdOrderByAssignedAtDesc(applicantId).stream()
				.map(this::toResponse).toList();
	}

	@Override
	public AssignmentResponseDto sendToOptician(Long assignmentId) {
		return transition(assignmentId, Assignment::sendToOptician);
	}

	@Override
	public AssignmentResponseDto returnFromOptician(Long assignmentId) {
		return transition(assignmentId, Assignment::returnFromOptician);
	}

	@Override
	public AssignmentResponseDto deliver(Long assignmentId) {
		return transition(assignmentId, Assignment::deliver);
	}

	/** Cada hito valida dentro de la entidad que el circuito esté en el punto que corresponde. */
	private AssignmentResponseDto transition(Long assignmentId, Consumer<Assignment> transition) {
		Assignment assignment = findAssignment(assignmentId);
		transition.accept(assignment);
		return toResponse(assignmentRepository.save(assignment));
	}

	@Override
	public AssignmentResponseDto cancel(Long assignmentId, String reason) {
		Assignment assignment = findAssignment(assignmentId);
		assignment.cancel(reason);
		return toResponse(assignmentRepository.save(assignment));
	}

	@Override
	public AssignmentDetailResponseDto buildDetail(AssignmentResponseDto assignment,
			ApplicantResponseDto applicant, FrameResponseDto frame, DonorResponseDto donor) {
		return new AssignmentDetailResponseDto(assignment, applicant, frame, donor);
	}

	@Override
	public long countDelivered(LocalDateTime from, LocalDateTime to) {
		return assignmentRepository.countDelivered(from, to);
	}

	@Override
	public long countDistinctApplicantsServed(LocalDateTime from, LocalDateTime to) {
		return assignmentRepository.countDistinctApplicantsServed(from, to);
	}

	@Override
	public List<MonthlyDeliveryResponseDto> deliveriesByMonth(LocalDateTime from, LocalDateTime to) {
		return assignmentRepository.deliveriesByMonth(from, to).stream()
				.map(row -> new MonthlyDeliveryResponseDto(YearMonth.from(row.getMonth()), row.getDeliveries(),
						row.getApplicants()))
				.toList();
	}

	private Assignment findAssignment(Long assignmentId) {
		return assignmentRepository.findById(assignmentId).orElseThrow(AssignmentNotFoundException::new);
	}

	private AssignmentResponseDto toResponse(Assignment assignment) {
		return new AssignmentResponseDto(assignment.getId(), assignment.getApplicantId(),
				assignment.getFrameId(), assignment.getPrescriptionId(), assignment.getAssignedAt(),
				assignment.getSentToOpticianAt(), assignment.getReturnedAt(), assignment.getDeliveredAt(),
				assignment.getCancelledAt(), assignment.getCancellationReason(), assignment.getNotes());
	}
}
