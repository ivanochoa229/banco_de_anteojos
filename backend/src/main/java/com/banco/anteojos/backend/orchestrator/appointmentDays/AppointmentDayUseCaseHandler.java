package com.banco.anteojos.backend.orchestrator.appointmentDays;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.banco.anteojos.backend.business.appointments.AppointmentDayService;
import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentDayRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentDayResponseDto;

import lombok.RequiredArgsConstructor;

/**
 * Los días de atención no pertenecen a nadie en particular: los configura cualquier integrante del
 * personal (el rol lo filtra SecurityConfig), así que no hay chequeo de pertenencia que hacer.
 */
@Component
@RequiredArgsConstructor
public class AppointmentDayUseCaseHandler implements AppointmentDayUseCaseOrchestrator {

	private final AppointmentDayService appointmentDayService;

	@Override
	public List<AppointmentDayResponseDto> listUpcomingDays() {
		return appointmentDayService.listUpcomingDays();
	}

	@Override
	public List<AppointmentDayResponseDto> listBookableDays() {
		return appointmentDayService.listBookableDays();
	}

	@Override
	public AppointmentDayResponseDto createDay(AppointmentDayRequestDto request) {
		return appointmentDayService.createDay(request);
	}

	// Transaccional por el lock del día: editar el cupo no puede cruzarse con una reserva en curso.
	@Override
	@Transactional
	public AppointmentDayResponseDto updateDay(Long appointmentDayId, AppointmentDayRequestDto request) {
		return appointmentDayService.updateDay(appointmentDayId, request);
	}

	@Override
	@Transactional
	public void deleteDay(Long appointmentDayId) {
		appointmentDayService.deleteDay(appointmentDayId);
	}
}
