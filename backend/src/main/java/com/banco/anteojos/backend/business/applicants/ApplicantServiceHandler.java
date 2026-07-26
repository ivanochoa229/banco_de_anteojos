package com.banco.anteojos.backend.business.applicants;

import java.util.List;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantUpdateRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionResponseDto;
import com.banco.anteojos.backend.business.applicants.entities.Applicant;
import com.banco.anteojos.backend.business.applicants.entities.Prescription;
import com.banco.anteojos.backend.business.applicants.exception.ApplicantNotFoundException;
import com.banco.anteojos.backend.business.applicants.exception.DniAlreadyExistsException;
import com.banco.anteojos.backend.persistence.applicants.ApplicantPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.applicants.PrescriptionPostgresSqlRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicantServiceHandler implements ApplicantService {

	private final ApplicantPostgresSqlRepository applicantRepository;
	private final PrescriptionPostgresSqlRepository prescriptionRepository;

	@Override
	public ApplicantResponseDto createApplicant(ApplicantCreationRequestDto request) {
		String dni = request.dni().trim();
		if (applicantRepository.findByDni(dni).isPresent()) {
			throw new DniAlreadyExistsException();
		}
		Applicant applicant = applicantRepository.save(new Applicant(request.firstName(), request.lastName(),
				dni, request.birthDate(), request.phone(), request.email()));
		return toResponse(applicant);
	}

	@Override
	public ApplicantResponseDto getApplicant(Long applicantId) {
		return toResponse(findApplicant(applicantId));
	}

	@Override
	public List<ApplicantResponseDto> listApplicants() {
		return applicantRepository.findAll().stream().map(this::toResponse).toList();
	}

	@Override
	public ApplicantResponseDto updateApplicant(Long applicantId, ApplicantUpdateRequestDto request) {
		Applicant applicant = findApplicant(applicantId);
		applicant.updatePersonalData(request.firstName(), request.lastName(), request.birthDate(),
				request.phone(), request.email());
		return toResponse(applicantRepository.save(applicant));
	}

	@Override
	public PrescriptionResponseDto addPrescription(Long applicantId, PrescriptionCreationRequestDto request) {
		findApplicant(applicantId);
		Prescription prescription = prescriptionRepository.save(new Prescription(applicantId,
				request.rightSphere(), request.rightCylinder(), request.rightAxis(),
				request.leftSphere(), request.leftCylinder(), request.leftAxis()));
		return toResponse(prescription);
	}

	@Override
	public List<PrescriptionResponseDto> listPrescriptions(Long applicantId) {
		findApplicant(applicantId);
		return prescriptionRepository.findByApplicantId(applicantId).stream().map(this::toResponse).toList();
	}

	private Applicant findApplicant(Long applicantId) {
		return applicantRepository.findById(applicantId).orElseThrow(ApplicantNotFoundException::new);
	}

	private ApplicantResponseDto toResponse(Applicant applicant) {
		return new ApplicantResponseDto(applicant.getId(), applicant.getFirstName(), applicant.getLastName(),
				applicant.getDni(), applicant.getCuil(), applicant.getBirthDate(), applicant.getPhone(),
				applicant.getEmail(), applicant.isIdentityValidated(), applicant.getCreatedAt());
	}

	private PrescriptionResponseDto toResponse(Prescription prescription) {
		return new PrescriptionResponseDto(prescription.getId(), prescription.getApplicantId(),
				prescription.getRightSphere(), prescription.getRightCylinder(), prescription.getRightAxis(),
				prescription.getLeftSphere(), prescription.getLeftCylinder(), prescription.getLeftAxis(),
				prescription.getCreatedAt());
	}
}
