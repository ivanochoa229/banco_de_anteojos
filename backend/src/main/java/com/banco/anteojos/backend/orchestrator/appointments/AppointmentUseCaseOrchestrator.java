package com.banco.anteojos.backend.orchestrator.appointments;

import java.time.LocalDate;
import java.util.List;

import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentCreationRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentReceiptUploadRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentReceiptResponseDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentResponseDto;

public interface AppointmentUseCaseOrchestrator {

	AppointmentResponseDto createAppointment(Long applicantId, AppointmentCreationRequestDto request);

	List<AppointmentResponseDto> listAppointmentsByApplicant(Long applicantId);

	List<AppointmentResponseDto> listAppointments(LocalDate date);

	AppointmentResponseDto getAppointment(Long appointmentId);

	AppointmentResponseDto reschedule(Long appointmentId, Long newAppointmentDayId);

	AppointmentResponseDto cancel(Long appointmentId, String reason);

	AppointmentResponseDto registerAttendance(Long appointmentId, boolean attended);

	/** Deja el comprobante cargado (PENDING_PAYMENT → PENDING_REVIEW), a la espera de revisión. */
	AppointmentResponseDto confirmAppointment(Long applicantId, Long appointmentId,
			AppointmentReceiptUploadRequestDto request);

	/** El administrativo aprueba el comprobante revisado (PENDING_REVIEW → SCHEDULED). */
	AppointmentResponseDto approve(Long appointmentId);

	AppointmentReceiptResponseDto getReceipt(Long appointmentId);
}
