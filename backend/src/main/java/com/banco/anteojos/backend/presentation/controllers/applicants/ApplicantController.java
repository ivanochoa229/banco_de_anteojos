package com.banco.anteojos.backend.presentation.controllers.applicants;

import org.springframework.data.domain.PageRequest;
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

import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantUpdateRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.EligibilityResponseDto;
import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.orchestrator.applicants.ApplicantUseCaseOrchestrator;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
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

	/** {@code ?q=} busca por apellido y nombre (sin importar tildes) o por el comienzo del DNI. */
	@GetMapping
	public PageResponseDto<ApplicantResponseDto> list(@RequestParam(defaultValue = "") String q,
			@RequestParam(defaultValue = "0") @PositiveOrZero int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return applicantOrchestrator.listApplicants(q, PageRequest.of(page, size));
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

	/** Para que el operador vea si falta la negativa de ANSES antes de intentar asignar. */
	@GetMapping("/{applicantId}/eligibility")
	public EligibilityResponseDto eligibility(@PathVariable Long applicantId) {
		return applicantOrchestrator.checkEligibility(applicantId);
	}
}
