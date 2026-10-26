package com.banco.anteojos.backend.orchestrator.applicants;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.banco.anteojos.backend.business.applicants.dto.request.AnsesCertificateUploadRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantProfileUpdateRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantUpdateRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionFileUploadRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.AnsesCertificateResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.EligibilityResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionFileResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionResponseDto;
import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;

public interface ApplicantUseCaseOrchestrator {

	ApplicantResponseDto createApplicant(ApplicantCreationRequestDto request);

	ApplicantResponseDto getApplicant(Long applicantId);

	PageResponseDto<ApplicantResponseDto> listApplicants(String search, Pageable pageable);

	ApplicantResponseDto updateApplicant(Long applicantId, ApplicantUpdateRequestDto request);

	ApplicantResponseDto updateProfile(Long applicantId, ApplicantProfileUpdateRequestDto request);

	PrescriptionResponseDto addPrescription(Long applicantId, PrescriptionCreationRequestDto request);

	List<PrescriptionResponseDto> listPrescriptions(Long applicantId);

	PrescriptionResponseDto uploadPrescriptionFile(Long applicantId, Long prescriptionId,
			PrescriptionFileUploadRequestDto request);

	PrescriptionFileResponseDto getPrescriptionFile(Long applicantId, Long prescriptionId);

	AnsesCertificateResponseDto uploadAnsesCertificate(Long applicantId, AnsesCertificateUploadRequestDto request);

	AnsesCertificateResponseDto getAnsesCertificate(Long applicantId);

	EligibilityResponseDto checkEligibility(Long applicantId);
}
