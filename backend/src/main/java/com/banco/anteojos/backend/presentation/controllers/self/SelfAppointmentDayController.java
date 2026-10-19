package com.banco.anteojos.backend.presentation.controllers.self;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentDayResponseDto;
import com.banco.anteojos.backend.orchestrator.appointmentDays.AppointmentDayUseCaseOrchestrator;

import lombok.RequiredArgsConstructor;

// El beneficiario solo ve los días en los que todavía puede pedir turno, no la configuración entera.
@RestController
@RequestMapping("/v1/me/appointment-days")
@RequiredArgsConstructor
public class SelfAppointmentDayController {

	private final AppointmentDayUseCaseOrchestrator appointmentDayOrchestrator;

	@GetMapping
	public List<AppointmentDayResponseDto> list() {
		return appointmentDayOrchestrator.listBookableDays();
	}
}
