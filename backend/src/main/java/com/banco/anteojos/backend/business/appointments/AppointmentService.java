package com.banco.anteojos.backend.business.appointments;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentCreationRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentReceiptUploadRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentReceiptResponseDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentResponseDto;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus;

public interface AppointmentService {

	/**
	 * La pertenencia de la asignación (si viene) ya la validó el orchestrator contra el dominio
	 * assignments: acá solo se persiste el turno.
	 */
	AppointmentResponseDto createAppointment(Long applicantId, AppointmentCreationRequestDto request);

	AppointmentResponseDto getAppointment(Long appointmentId);

	/** Con {@code date} devuelve la agenda de ese día; sin ella, todos los turnos. */
	List<AppointmentResponseDto> listAppointments(LocalDate date);

	List<AppointmentResponseDto> listAppointmentsByApplicant(Long applicantId);

	AppointmentResponseDto reschedule(Long appointmentId, LocalDateTime newScheduledAt);

	AppointmentResponseDto cancel(Long appointmentId, String reason);

	AppointmentResponseDto registerAttendance(Long appointmentId, boolean attended);

	/** Turnos en un estado final dentro del rango, para el panel de indicadores (RF-26). */
	long countByStatus(AppointmentStatus status, LocalDateTime from, LocalDateTime to);

	/** Deja el comprobante cargado (PENDING_PAYMENT → PENDING_REVIEW), a la espera de revisión. */
	AppointmentResponseDto submitReceiptForReview(Long appointmentId, AppointmentReceiptUploadRequestDto request);

	/** El administrativo aprueba el comprobante revisado (PENDING_REVIEW → SCHEDULED). */
	AppointmentResponseDto approve(Long appointmentId);

	AppointmentReceiptResponseDto getReceipt(Long appointmentId);
}
