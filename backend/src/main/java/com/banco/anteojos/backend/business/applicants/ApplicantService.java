package com.banco.anteojos.backend.business.applicants;

import java.util.List;

import com.banco.anteojos.backend.business.applicants.dto.request.AnsesCertificateUploadRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantUpdateRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionFileUploadRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.AnsesCertificateResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.EligibilityResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionFileResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionResponseDto;

public interface ApplicantService {

	ApplicantResponseDto createApplicant(ApplicantCreationRequestDto request);

	ApplicantResponseDto getApplicant(Long applicantId);

	List<ApplicantResponseDto> listApplicants();

	ApplicantResponseDto updateApplicant(Long applicantId, ApplicantUpdateRequestDto request);

	PrescriptionResponseDto addPrescription(Long applicantId, PrescriptionCreationRequestDto request);

	List<PrescriptionResponseDto> listPrescriptions(Long applicantId);

	/** Falla si la receta no existe o no es del solicitante: valida pertenencia. */
	PrescriptionResponseDto getPrescription(Long applicantId, Long prescriptionId);

	EligibilityResponseDto checkEligibility(Long applicantId);

	PrescriptionResponseDto uploadPrescriptionFile(Long applicantId, Long prescriptionId,
			PrescriptionFileUploadRequestDto request);

	PrescriptionFileResponseDto getPrescriptionFile(Long applicantId, Long prescriptionId);

	AnsesCertificateResponseDto uploadAnsesCertificate(Long applicantId, AnsesCertificateUploadRequestDto request);

	AnsesCertificateResponseDto getAnsesCertificate(Long applicantId);
}
