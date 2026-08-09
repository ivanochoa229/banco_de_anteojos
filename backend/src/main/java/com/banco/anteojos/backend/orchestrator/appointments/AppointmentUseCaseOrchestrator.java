package com.banco.anteojos.backend.orchestrator.appointments;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentCreationRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentResponseDto;

public interface AppointmentUseCaseOrchestrator {

	AppointmentResponseDto createAppointment(Long applicantId, AppointmentCreationRequestDto request);

	List<AppointmentResponseDto> listAppointmentsByApplicant(Long applicantId);

	List<AppointmentResponseDto> listAppointments(LocalDate date);

	AppointmentResponseDto getAppointment(Long appointmentId);

	AppointmentResponseDto reschedule(Long appointmentId, LocalDateTime newScheduledAt);

	AppointmentResponseDto cancel(Long appointmentId, String reason);

	AppointmentResponseDto registerAttendance(Long appointmentId, boolean attended);
}
