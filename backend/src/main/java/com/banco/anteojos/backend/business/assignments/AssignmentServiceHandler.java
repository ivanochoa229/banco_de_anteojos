package com.banco.anteojos.backend.business.assignments;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.request.AssignmentCreationRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.request.DeliveryPhotoUploadRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentCreationResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentDeliveryResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentDetailResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.DeliveryPhotoResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.MonthlyDeliveryResponseDto;
import com.banco.anteojos.backend.business.assignments.entities.Assignment;
import com.banco.anteojos.backend.business.assignments.exception.AssignmentNotFoundException;
import com.banco.anteojos.backend.business.assignments.exception.DeliveryPhotoNotFoundException;
import com.banco.anteojos.backend.business.assignments.exception.InvalidDeliveryPhotoException;
import com.banco.anteojos.backend.business.donors.dto.response.DonorResponseDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.persistence.assignments.AssignmentPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.notifications.EmailAttachment;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssignmentServiceHandler implements AssignmentService {

	// Foto sacada con el celular en la sede: lo que generan las cámaras y los navegadores.
	private static final Map<String, String> ALLOWED_PHOTO_TYPES = Map.of(
			"image/jpeg", "jpg",
			"image/png", "png",
			"image/webp", "webp");

	private final AssignmentPostgresSqlRepository assignmentRepository;
	private final R2StorageClient r2StorageClient;

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
	public AssignmentDeliveryResponseDto deliver(Long assignmentId, DeliveryPhotoUploadRequestDto photo,
			boolean photoConsent) {
		Assignment assignment = findAssignment(assignmentId);
		// La foto se valida antes de tocar nada, y la transición antes de subirla: ni un
		// archivo inválido ni una entrega fuera de orden dejan un huérfano en R2.
		String extension = photo == null ? null : validatePhoto(photo);
		assignment.deliver();
		if (photo == null) {
			return new AssignmentDeliveryResponseDto(toResponse(assignmentRepository.save(assignment)), null);
		}

		String contentType = normalizeContentType(photo.contentType());
		String key = "deliveries/%d/%s.%s".formatted(assignmentId, UUID.randomUUID(), extension);
		r2StorageClient.upload(key, photo.content(), contentType);
		assignment.attachDeliveryPhoto(key, contentType, photo.originalName(), photoConsent);

		AssignmentResponseDto response = toResponse(assignmentRepository.save(assignment));
		// Sin el permiso del beneficiario la foto queda en el registro pero no sale de la fundación.
		EmailAttachment photoForDonor = photoConsent
				? new EmailAttachment(photo.content(), contentType, photo.originalName())
				: null;
		return new AssignmentDeliveryResponseDto(response, photoForDonor);
	}

	@Override
	public DeliveryPhotoResponseDto getDeliveryPhoto(Long assignmentId) {
		Assignment assignment = findAssignment(assignmentId);
		if (!assignment.hasDeliveryPhoto()) {
			throw new DeliveryPhotoNotFoundException();
		}
		PresignedUrl presigned = r2StorageClient.presignedGetUrl(assignment.getDeliveryPhotoKey());
		return new DeliveryPhotoResponseDto(presigned.url(), presigned.expiresAt(),
				assignment.getDeliveryPhotoOriginalName(), assignment.getDeliveryPhotoContentType());
	}

	/** Devuelve la extensión que le corresponde al tipo de archivo. */
	private String validatePhoto(DeliveryPhotoUploadRequestDto photo) {
		if (photo.content() == null || photo.content().length == 0) {
			throw new InvalidDeliveryPhotoException("El archivo está vacío");
		}
		String extension = ALLOWED_PHOTO_TYPES.get(normalizeContentType(photo.contentType()));
		if (extension == null) {
			throw new InvalidDeliveryPhotoException("La foto de la entrega debe ser JPG, PNG o WEBP");
		}
		return extension;
	}

	// El navegador puede mandar parámetros ("image/jpeg; charset=..."): queda solo el tipo.
	private String normalizeContentType(String contentType) {
		if (contentType == null) {
			return "";
		}
		return contentType.split(";")[0].trim().toLowerCase();
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
				assignment.getCancelledAt(), assignment.getCancellationReason(), assignment.getNotes(),
				assignment.getDeliveryPhotoOriginalName(), assignment.isDeliveryPhotoConsent());
	}
}
