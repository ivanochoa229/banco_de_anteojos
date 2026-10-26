package com.banco.anteojos.backend.business.applicants;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

public interface ApplicantService {

	ApplicantResponseDto createApplicant(ApplicantCreationRequestDto request);

	ApplicantResponseDto getApplicant(Long applicantId);

	/** {@code search} filtra por apellido y nombre o por DNI; null o en blanco no filtra. */
	PageResponseDto<ApplicantResponseDto> listApplicants(String search, Pageable pageable);

	/** "Apellido, Nombre" por id, para mostrar en listados de otros dominios. Los ids inexistentes no vienen. */
	Map<Long, String> getFullNames(Collection<Long> applicantIds);

	ApplicantResponseDto updateApplicant(Long applicantId, ApplicantUpdateRequestDto request);

	/** Autogestión del beneficiario: no toca el email, que es su usuario de ingreso. */
	ApplicantResponseDto updateProfile(Long applicantId, ApplicantProfileUpdateRequestDto request);

	PrescriptionResponseDto addPrescription(Long applicantId, PrescriptionCreationRequestDto request);

	/**
	 * Receta cargada por el propio beneficiario: solo el archivo (PDF o foto), sin graduación.
	 * Se valida y se sube antes de guardar, así un archivo rechazado no deja una receta vacía.
	 */
	PrescriptionResponseDto addPrescriptionFromFile(Long applicantId, PrescriptionFileUploadRequestDto request);

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
