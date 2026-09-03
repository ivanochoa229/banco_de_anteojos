package com.banco.anteojos.backend.business.appointments;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentCreationRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentReceiptUploadRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentReceiptResponseDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentResponseDto;
import com.banco.anteojos.backend.business.appointments.entities.Appointment;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus;
import com.banco.anteojos.backend.business.appointments.exception.AppointmentNotFoundException;
import com.banco.anteojos.backend.business.appointments.exception.AppointmentReceiptNotFoundException;
import com.banco.anteojos.backend.business.appointments.exception.InvalidAppointmentReceiptException;
import com.banco.anteojos.backend.persistence.appointments.AppointmentPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentServiceHandler implements AppointmentService {

	// Mismo criterio que la receta del solicitante: PDF o foto del comprobante.
	private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
			"application/pdf", "pdf",
			"image/jpeg", "jpg",
			"image/png", "png");

	private final AppointmentPostgresSqlRepository appointmentRepository;
	private final R2StorageClient r2StorageClient;

	@Override
	public AppointmentResponseDto createAppointment(Long applicantId, AppointmentCreationRequestDto request) {
		Appointment appointment = appointmentRepository.save(new Appointment(applicantId,
				request.assignmentId(), request.scheduledAt(), request.notes()));
		return toResponse(appointment);
	}

	@Override
	public AppointmentResponseDto getAppointment(Long appointmentId) {
		return toResponse(findAppointment(appointmentId));
	}

	@Override
	public List<AppointmentResponseDto> listAppointments(LocalDate date) {
		List<Appointment> appointments = date == null
				? appointmentRepository.findAllByOrderByScheduledAtDesc()
				: appointmentRepository.findByDay(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
		return appointments.stream().map(this::toResponse).toList();
	}

	@Override
	public List<AppointmentResponseDto> listAppointmentsByApplicant(Long applicantId) {
		return appointmentRepository.findByApplicantIdOrderByScheduledAtDesc(applicantId).stream()
				.map(this::toResponse).toList();
	}

	@Override
	public AppointmentResponseDto reschedule(Long appointmentId, LocalDateTime newScheduledAt) {
		Appointment appointment = findAppointment(appointmentId);
		appointment.reschedule(newScheduledAt);
		return toResponse(appointmentRepository.save(appointment));
	}

	@Override
	public AppointmentResponseDto cancel(Long appointmentId, String reason) {
		Appointment appointment = findAppointment(appointmentId);
		appointment.cancel(reason);
		return toResponse(appointmentRepository.save(appointment));
	}

	@Override
	public AppointmentResponseDto registerAttendance(Long appointmentId, boolean attended) {
		Appointment appointment = findAppointment(appointmentId);
		appointment.registerAttendance(attended);
		return toResponse(appointmentRepository.save(appointment));
	}

	@Override
	public long countByStatus(AppointmentStatus status, LocalDateTime from, LocalDateTime to) {
		return appointmentRepository.countByStatusAndScheduledAtBetween(status, from, to);
	}

	@Override
	public AppointmentResponseDto confirmWithReceipt(Long appointmentId, AppointmentReceiptUploadRequestDto request) {
		Appointment appointment = findAppointment(appointmentId);
		String extension = validateFile(request);

		String key = "appointments/%d/%s.%s".formatted(appointmentId, UUID.randomUUID(), extension);
		String contentType = normalizeContentType(request.contentType());
		r2StorageClient.upload(key, request.content(), contentType);

		// Exigir PENDING_PAYMENT acá adentro evita que una segunda subida pise el comprobante
		// de un turno ya confirmado: el segundo intento falla con 409 antes de tocar nada.
		appointment.confirmWithReceipt(key, contentType, request.originalName());
		return toResponse(appointmentRepository.save(appointment));
	}

	@Override
	public AppointmentReceiptResponseDto getReceipt(Long appointmentId) {
		Appointment appointment = findAppointment(appointmentId);
		if (appointment.getReceiptKey() == null) {
			throw new AppointmentReceiptNotFoundException();
		}
		PresignedUrl presigned = r2StorageClient.presignedGetUrl(appointment.getReceiptKey());
		return new AppointmentReceiptResponseDto(presigned.url(), presigned.expiresAt(),
				appointment.getReceiptOriginalName(), appointment.getReceiptContentType());
	}

	private String validateFile(AppointmentReceiptUploadRequestDto request) {
		if (request.content() == null || request.content().length == 0) {
			throw new InvalidAppointmentReceiptException("El archivo está vacío");
		}
		String extension = ALLOWED_CONTENT_TYPES.get(normalizeContentType(request.contentType()));
		if (extension == null) {
			throw new InvalidAppointmentReceiptException("El archivo debe ser PDF, JPG o PNG");
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

	private Appointment findAppointment(Long appointmentId) {
		return appointmentRepository.findById(appointmentId).orElseThrow(AppointmentNotFoundException::new);
	}

	private AppointmentResponseDto toResponse(Appointment appointment) {
		return new AppointmentResponseDto(appointment.getId(), appointment.getApplicantId(),
				appointment.getAssignmentId(), appointment.getScheduledAt(),
				appointment.getStatus().name(), appointment.getNotes(),
				appointment.getCancellationReason(), appointment.getCreatedAt(),
				appointment.getReceiptOriginalName());
	}
}
