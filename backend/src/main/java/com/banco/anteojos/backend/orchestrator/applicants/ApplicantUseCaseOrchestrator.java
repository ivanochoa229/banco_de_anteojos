package com.banco.anteojos.backend.orchestrator.applicants;

import java.util.List;

import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantUpdateRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionResponseDto;

public interface ApplicantUseCaseOrchestrator {

	ApplicantResponseDto createApplicant(ApplicantCreationRequestDto request);

	ApplicantResponseDto getApplicant(Long applicantId);

	List<ApplicantResponseDto> listApplicants();

	ApplicantResponseDto updateApplicant(Long applicantId, ApplicantUpdateRequestDto request);

	PrescriptionResponseDto addPrescription(Long applicantId, PrescriptionCreationRequestDto request);

	List<PrescriptionResponseDto> listPrescriptions(Long applicantId);
}
