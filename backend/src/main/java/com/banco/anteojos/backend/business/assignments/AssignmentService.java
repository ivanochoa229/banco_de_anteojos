package com.banco.anteojos.backend.business.assignments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.request.AssignmentCreationRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.request.DeliveryPhotoUploadRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentCreationResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentDeliveryResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentDetailResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentListItemResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.DeliveryPhotoResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.MonthlyDeliveryResponseDto;
import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
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

	PageResponseDto<AssignmentResponseDto> listAssignments(boolean onlyLive, Pageable pageable);

	/**
	 * Arma las filas de la cola con los nombres que el orchestrator ya resolvió en applicants y
	 * frames, indexados por id.
	 */
	PageResponseDto<AssignmentListItemResponseDto> buildListItems(PageResponseDto<AssignmentResponseDto> assignments,
			Map<Long, String> applicantNames, Map<Long, String> frameSealCodes);

	List<AssignmentResponseDto> listAssignmentsByApplicant(Long applicantId);

	AssignmentResponseDto sendToOptician(Long assignmentId);

	AssignmentResponseDto returnFromOptician(Long assignmentId);

	/**
	 * @param photo foto del beneficiario con sus anteojos, o null si no la sacaron.
	 * @param photoConsent si el beneficiario autorizó mandarle la foto al donante.
	 */
	AssignmentDeliveryResponseDto deliver(Long assignmentId, DeliveryPhotoUploadRequestDto photo,
			boolean photoConsent);

	DeliveryPhotoResponseDto getDeliveryPhoto(Long assignmentId);

	AssignmentResponseDto cancel(Long assignmentId, String reason);

	/** Arma la trazabilidad con los datos que el orchestrator ya resolvió en cada dominio. */
	AssignmentDetailResponseDto buildDetail(AssignmentResponseDto assignment, ApplicantResponseDto applicant,
			FrameResponseDto frame, DonorResponseDto donor);

	// Para el panel de indicadores (RF-26/27).

	long countDelivered(LocalDateTime from, LocalDateTime to);

	long countDistinctApplicantsServed(LocalDateTime from, LocalDateTime to);

	List<MonthlyDeliveryResponseDto> deliveriesByMonth(LocalDateTime from, LocalDateTime to);
}
