package com.banco.anteojos.backend.presentation.controllers.applicants;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentCreationRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentResponseDto;
import com.banco.anteojos.backend.orchestrator.appointments.AppointmentUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// El turno se crea colgando del beneficiario, que es el dueño del recurso.
@RestController
@RequestMapping("/v1/applicants/{applicantId}/appointments")
@RequiredArgsConstructor
public class ApplicantAppointmentController {

	private final AppointmentUseCaseOrchestrator appointmentOrchestrator;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AppointmentResponseDto create(@PathVariable Long applicantId,
			@Valid @RequestBody AppointmentCreationRequestDto request) {
		return appointmentOrchestrator.createAppointment(applicantId, request);
	}

	@GetMapping
	public List<AppointmentResponseDto> list(@PathVariable Long applicantId) {
		return appointmentOrchestrator.listAppointmentsByApplicant(applicantId);
	}
}
