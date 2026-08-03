package com.banco.anteojos.backend.orchestrator.applicants;

import java.util.List;

import org.springframework.stereotype.Component;

import com.banco.anteojos.backend.business.applicants.ApplicantService;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantUpdateRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionFileUploadRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionFileResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionResponseDto;

import lombok.RequiredArgsConstructor;

// La existencia del applicant (pertenencia de la receta) la valida el service como primer paso.
@Component
@RequiredArgsConstructor
public class ApplicantUseCaseHandler implements ApplicantUseCaseOrchestrator {

	private final ApplicantService applicantService;

	@Override
	public ApplicantResponseDto createApplicant(ApplicantCreationRequestDto request) {
		return applicantService.createApplicant(request);
	}

	@Override
	public ApplicantResponseDto getApplicant(Long applicantId) {
		return applicantService.getApplicant(applicantId);
	}

	@Override
	public List<ApplicantResponseDto> listApplicants() {
		return applicantService.listApplicants();
	}

	@Override
	public ApplicantResponseDto updateApplicant(Long applicantId, ApplicantUpdateRequestDto request) {
		return applicantService.updateApplicant(applicantId, request);
	}

	@Override
	public PrescriptionResponseDto addPrescription(Long applicantId, PrescriptionCreationRequestDto request) {
		return applicantService.addPrescription(applicantId, request);
	}

	@Override
	public List<PrescriptionResponseDto> listPrescriptions(Long applicantId) {
		return applicantService.listPrescriptions(applicantId);
	}

	@Override
	public PrescriptionResponseDto uploadPrescriptionFile(Long applicantId, Long prescriptionId,
			PrescriptionFileUploadRequestDto request) {
		return applicantService.uploadPrescriptionFile(applicantId, prescriptionId, request);
	}

	@Override
	public PrescriptionFileResponseDto getPrescriptionFile(Long applicantId, Long prescriptionId) {
		return applicantService.getPrescriptionFile(applicantId, prescriptionId);
	}
}
