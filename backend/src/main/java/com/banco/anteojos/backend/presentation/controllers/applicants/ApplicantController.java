package com.banco.anteojos.backend.presentation.controllers.applicants;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantUpdateRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.orchestrator.applicants.ApplicantUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/applicants")
@RequiredArgsConstructor
public class ApplicantController {

	private final ApplicantUseCaseOrchestrator applicantOrchestrator;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApplicantResponseDto create(@Valid @RequestBody ApplicantCreationRequestDto request) {
		return applicantOrchestrator.createApplicant(request);
	}

	@GetMapping
	public List<ApplicantResponseDto> list() {
		return applicantOrchestrator.listApplicants();
	}

	@GetMapping("/{applicantId}")
	public ApplicantResponseDto get(@PathVariable Long applicantId) {
		return applicantOrchestrator.getApplicant(applicantId);
	}

	@PutMapping("/{applicantId}")
	public ApplicantResponseDto update(@PathVariable Long applicantId,
			@Valid @RequestBody ApplicantUpdateRequestDto request) {
		return applicantOrchestrator.updateApplicant(applicantId, request);
	}
}
