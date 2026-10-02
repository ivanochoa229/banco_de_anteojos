package com.banco.anteojos.backend.orchestrator.appointmentDays;

import java.util.List;

import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentDayRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentDayResponseDto;

public interface AppointmentDayUseCaseOrchestrator {

	List<AppointmentDayResponseDto> listUpcomingDays();

	List<AppointmentDayResponseDto> listBookableDays();

	AppointmentDayResponseDto createDay(AppointmentDayRequestDto request);

	AppointmentDayResponseDto updateDay(Long appointmentDayId, AppointmentDayRequestDto request);

	void deleteDay(Long appointmentDayId);
}
