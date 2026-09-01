package com.banco.anteojos.backend.presentation.controllers.appointments;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentAttendanceRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentCancellationRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentRescheduleRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentReceiptResponseDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentResponseDto;
import com.banco.anteojos.backend.orchestrator.appointments.AppointmentUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Como en assignments, cada acción sobre el turno es un sub-recurso en sustantivo y PUT:
 * la agenda ({@code /schedule}) se reemplaza al reprogramar, y repetir una cancelación o una
 * asistencia no duplica nada.
 */
@RestController
@RequestMapping("/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

	private final AppointmentUseCaseOrchestrator appointmentOrchestrator;

	/** Con {@code ?date=} devuelve la agenda de ese día ordenada por hora. */
	@GetMapping
	public List<AppointmentResponseDto> list(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return appointmentOrchestrator.listAppointments(date);
	}

	@GetMapping("/{appointmentId}")
	public AppointmentResponseDto get(@PathVariable Long appointmentId) {
		return appointmentOrchestrator.getAppointment(appointmentId);
	}

	/** Reprogramación (RF-21): pisa la fecha del mismo turno, no crea otro. */
	@PutMapping("/{appointmentId}/schedule")
	public AppointmentResponseDto reschedule(@PathVariable Long appointmentId,
			@Valid @RequestBody AppointmentRescheduleRequestDto request) {
		return appointmentOrchestrator.reschedule(appointmentId, request.scheduledAt());
	}

	@PutMapping("/{appointmentId}/cancellation")
	public AppointmentResponseDto cancel(@PathVariable Long appointmentId,
			@Valid @RequestBody AppointmentCancellationRequestDto request) {
		return appointmentOrchestrator.cancel(appointmentId, request.reason());
	}

	/** El día del turno: asistió (COMPLETED) o faltó (MISSED). */
	@PutMapping("/{appointmentId}/attendance")
	public AppointmentResponseDto registerAttendance(@PathVariable Long appointmentId,
			@Valid @RequestBody AppointmentAttendanceRequestDto request) {
		return appointmentOrchestrator.registerAttendance(appointmentId, request.attended());
	}

	/** Para que el operador revise offline el comprobante del bono contribución. */
	@GetMapping("/{appointmentId}/receipt")
	public AppointmentReceiptResponseDto getReceipt(@PathVariable Long appointmentId) {
		return appointmentOrchestrator.getReceipt(appointmentId);
	}
}
