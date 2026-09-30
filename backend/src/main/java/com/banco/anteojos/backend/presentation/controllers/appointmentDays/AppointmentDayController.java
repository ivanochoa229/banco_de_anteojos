package com.banco.anteojos.backend.presentation.controllers.appointmentDays;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentDayRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentDayResponseDto;
import com.banco.anteojos.backend.orchestrator.appointmentDays.AppointmentDayUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Días en que la sede atiende (RF-20): el personal los habilita con su horario y su cupo. */
@RestController
@RequestMapping("/v1/appointment-days")
@RequiredArgsConstructor
public class AppointmentDayController {

	private final AppointmentDayUseCaseOrchestrator appointmentDayOrchestrator;

	/** De hoy en adelante, con las franjas tomadas y libres de cada día. */
	@GetMapping
	public List<AppointmentDayResponseDto> list() {
		return appointmentDayOrchestrator.listUpcomingDays();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AppointmentDayResponseDto create(@Valid @RequestBody AppointmentDayRequestDto request) {
		return appointmentDayOrchestrator.createDay(request);
	}

	@PutMapping("/{appointmentDayId}")
	public AppointmentDayResponseDto update(@PathVariable Long appointmentDayId,
			@Valid @RequestBody AppointmentDayRequestDto request) {
		return appointmentDayOrchestrator.updateDay(appointmentDayId, request);
	}

	@DeleteMapping("/{appointmentDayId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long appointmentDayId) {
		appointmentDayOrchestrator.deleteDay(appointmentDayId);
	}
}
