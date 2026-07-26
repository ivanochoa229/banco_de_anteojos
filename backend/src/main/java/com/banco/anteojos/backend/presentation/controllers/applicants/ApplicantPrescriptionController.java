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

import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionResponseDto;
import com.banco.anteojos.backend.orchestrator.applicants.ApplicantUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/applicants/{applicantId}/prescriptions")
@RequiredArgsConstructor
public class ApplicantPrescriptionController {

	private final ApplicantUseCaseOrchestrator applicantOrchestrator;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PrescriptionResponseDto create(@PathVariable Long applicantId,
			@Valid @RequestBody PrescriptionCreationRequestDto request) {
		return applicantOrchestrator.addPrescription(applicantId, request);
	}

	@GetMapping
	public List<PrescriptionResponseDto> list(@PathVariable Long applicantId) {
		return applicantOrchestrator.listPrescriptions(applicantId);
	}
}
