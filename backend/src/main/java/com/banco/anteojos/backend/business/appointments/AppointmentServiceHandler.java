package com.banco.anteojos.backend.business.appointments;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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

	private static final byte[] PDF_SIGNATURE = { '%', 'P', 'D', 'F', '-' };
	private static final byte[] JPEG_SIGNATURE = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF };
	private static final byte[] PNG_SIGNATURE =
			{ (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n' };

	// Solo para PDF: la foto no da texto para chequear (queda a criterio del operador, que ve
	// el comprobante antes del turno, igual que la validación final de RENAPER/ANSES).
	private static final Pattern RECEIPT_KEYWORDS = Pattern.compile(
			"comprobante|recibo|bono|contribuci[oó]n|transacci[oó]n|pago|monto|total",
			Pattern.CASE_INSENSITIVE);

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
		// El content-type lo declara el navegador; validamos los bytes reales para que un
		// archivo renombrado (ej. .exe pasado como .pdf) no pase el filtro.
		if (!matchesSignature(request.content(), extension)) {
			throw new InvalidAppointmentReceiptException("El archivo no es un PDF, JPG o PNG válido");
		}
		if (extension.equals("pdf")) {
			requireReceiptLikeText(request.content());
		}
		return extension;
	}

	private void requireReceiptLikeText(byte[] content) {
		String text;
		try (PDDocument document = Loader.loadPDF(content)) {
			text = new PDFTextStripper().getText(document);
		} catch (IOException e) {
			throw new InvalidAppointmentReceiptException("El PDF no se pudo leer");
		}
		if (!RECEIPT_KEYWORDS.matcher(text).find()) {
			throw new InvalidAppointmentReceiptException("El PDF no parece ser un comprobante de pago");
		}
	}

	private boolean matchesSignature(byte[] content, String extension) {
		return switch (extension) {
			case "pdf" -> startsWith(content, PDF_SIGNATURE);
			case "jpg" -> startsWith(content, JPEG_SIGNATURE);
			case "png" -> startsWith(content, PNG_SIGNATURE);
			default -> false;
		};
	}

	private boolean startsWith(byte[] content, byte[] signature) {
		if (content.length < signature.length) {
			return false;
		}
		for (int i = 0; i < signature.length; i++) {
			if (content[i] != signature[i]) {
				return false;
			}
		}
		return true;
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
