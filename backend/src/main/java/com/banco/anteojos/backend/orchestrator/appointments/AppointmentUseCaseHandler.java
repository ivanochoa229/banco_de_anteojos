package com.banco.anteojos.backend.orchestrator.appointments;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.banco.anteojos.backend.business.applicants.ApplicantService;
import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.appointments.AppointmentService;
import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentCreationRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentReceiptUploadRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentReceiptResponseDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentResponseDto;
import com.banco.anteojos.backend.business.appointments.exception.AppointmentNotFoundException;
import com.banco.anteojos.backend.business.assignments.AssignmentService;
import com.banco.anteojos.backend.business.assignments.exception.AssignmentNotFoundException;
import com.banco.anteojos.backend.thirdPartyServiceComunication.notifications.NotificationClient;

import lombok.RequiredArgsConstructor;

/**
 * El turno nace PENDING_PAYMENT y recién dispara la notificación de RF-22 cuando se confirma con
 * el comprobante (confirmAppointment), no al crearlo. Reprogramación y cancelación siguen
 * disparando la suya después de persistir: el cliente de notificaciones es best effort y nunca
 * corta el caso de uso.
 */
@Component
@RequiredArgsConstructor
public class AppointmentUseCaseHandler implements AppointmentUseCaseOrchestrator {

	private final AppointmentService appointmentService;
	private final ApplicantService applicantService;
	private final AssignmentService assignmentService;
	private final NotificationClient notificationClient;

	@Override
	public AppointmentResponseDto createAppointment(Long applicantId, AppointmentCreationRequestDto request) {
		// Pertenencia primero: que la asignación a retirar sea de este beneficiario. 404 y no
		// 403 para no revelar de quién es la asignación ajena.
		applicantService.getApplicant(applicantId);
		if (request.assignmentId() != null
				&& !assignmentService.getAssignment(request.assignmentId()).applicantId().equals(applicantId)) {
			throw new AssignmentNotFoundException();
		}
		return appointmentService.createAppointment(applicantId, request);
	}

	@Override
	public List<AppointmentResponseDto> listAppointmentsByApplicant(Long applicantId) {
		applicantService.getApplicant(applicantId);
		return appointmentService.listAppointmentsByApplicant(applicantId);
	}

	@Override
	public List<AppointmentResponseDto> listAppointments(LocalDate date) {
		return appointmentService.listAppointments(date);
	}

	@Override
	public AppointmentResponseDto getAppointment(Long appointmentId) {
		return appointmentService.getAppointment(appointmentId);
	}

	@Override
	public AppointmentResponseDto reschedule(Long appointmentId, LocalDateTime newScheduledAt) {
		AppointmentResponseDto appointment = appointmentService.reschedule(appointmentId, newScheduledAt);
		ApplicantResponseDto applicant = applicantService.getApplicant(appointment.applicantId());
		notificationClient.sendAppointmentRescheduled(applicant.email(), applicant.firstName(),
				appointment.scheduledAt());
		return appointment;
	}

	@Override
	public AppointmentResponseDto cancel(Long appointmentId, String reason) {
		AppointmentResponseDto appointment = appointmentService.cancel(appointmentId, reason);
		ApplicantResponseDto applicant = applicantService.getApplicant(appointment.applicantId());
		notificationClient.sendAppointmentCancelled(applicant.email(), applicant.firstName(),
				appointment.scheduledAt());
		return appointment;
	}

	@Override
	public AppointmentResponseDto registerAttendance(Long appointmentId, boolean attended) {
		return appointmentService.registerAttendance(appointmentId, attended);
	}

	@Override
	public AppointmentResponseDto confirmAppointment(Long applicantId, Long appointmentId,
			AppointmentReceiptUploadRequestDto request) {
		// Pertenencia primero: 404 y no 403 para no revelar de quién es el turno ajeno.
		if (!appointmentService.getAppointment(appointmentId).applicantId().equals(applicantId)) {
			throw new AppointmentNotFoundException();
		}
		AppointmentResponseDto appointment = appointmentService.confirmWithReceipt(appointmentId, request);
		ApplicantResponseDto applicant = applicantService.getApplicant(applicantId);
		notificationClient.sendAppointmentScheduled(applicant.email(), applicant.firstName(),
				appointment.scheduledAt());
		return appointment;
	}

	@Override
	public AppointmentReceiptResponseDto getReceipt(Long appointmentId) {
		return appointmentService.getReceipt(appointmentId);
	}
}
