package com.banco.anteojos.backend.presentation.controllers.self;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentCreationRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentReceiptUploadRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentResponseDto;
import com.banco.anteojos.backend.business.appointments.exception.InvalidAppointmentReceiptException;
import com.banco.anteojos.backend.business.security.CurrentUserService;
import com.banco.anteojos.backend.orchestrator.appointments.AppointmentUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// El applicantId nunca viene del cliente: sale del JWT vía CurrentUserService. Reusa el mismo
// orchestrator que usa el operador, así que la validación de pertenencia del assignmentId de
// retiro y la notificación de RF-22 se aplican igual.
@RestController
@RequestMapping("/v1/me/appointments")
@RequiredArgsConstructor
public class SelfAppointmentController {

	private final AppointmentUseCaseOrchestrator appointmentOrchestrator;
	private final CurrentUserService currentUserService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AppointmentResponseDto create(@Valid @RequestBody AppointmentCreationRequestDto request) {
		return appointmentOrchestrator.createAppointment(currentUserService.getApplicantId(), request);
	}

	@GetMapping
	public List<AppointmentResponseDto> list() {
		return appointmentOrchestrator.listAppointmentsByApplicant(currentUserService.getApplicantId());
	}

	// PUT y no POST: confirma el turno con el comprobante del bono contribución.
	@PutMapping("/{appointmentId}/receipt")
	public AppointmentResponseDto confirm(@PathVariable Long appointmentId,
			@RequestParam("file") MultipartFile file) {
		return appointmentOrchestrator.confirmAppointment(currentUserService.getApplicantId(), appointmentId,
				toUploadRequest(file));
	}

	private AppointmentReceiptUploadRequestDto toUploadRequest(MultipartFile file) {
		try {
			return new AppointmentReceiptUploadRequestDto(file.getBytes(), file.getContentType(),
					file.getOriginalFilename());
		} catch (IOException e) {
			throw new InvalidAppointmentReceiptException("No se pudo leer el archivo");
		}
	}
}
