package com.banco.anteojos.backend.business.applicants;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantUpdateRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionFileUploadRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionFileResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionResponseDto;
import com.banco.anteojos.backend.business.applicants.entities.Applicant;
import com.banco.anteojos.backend.business.applicants.entities.Prescription;
import com.banco.anteojos.backend.business.applicants.exception.ApplicantNotFoundException;
import com.banco.anteojos.backend.business.applicants.exception.DniAlreadyExistsException;
import com.banco.anteojos.backend.business.applicants.exception.InvalidPrescriptionFileException;
import com.banco.anteojos.backend.business.applicants.exception.PrescriptionFileNotFoundException;
import com.banco.anteojos.backend.business.applicants.exception.PrescriptionNotFoundException;
import com.banco.anteojos.backend.persistence.applicants.ApplicantPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.applicants.PrescriptionPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicantServiceHandler implements ApplicantService {

	// La receta llega escaneada o fotografiada por el operador; nada más tiene sentido acá.
	private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
			"application/pdf", "pdf",
			"image/jpeg", "jpg",
			"image/png", "png");

	private final ApplicantPostgresSqlRepository applicantRepository;
	private final PrescriptionPostgresSqlRepository prescriptionRepository;
	private final R2StorageClient r2StorageClient;

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

	@Override
	public PrescriptionResponseDto uploadPrescriptionFile(Long applicantId, Long prescriptionId,
			PrescriptionFileUploadRequestDto request) {
		Prescription prescription = findPrescription(applicantId, prescriptionId);
		String extension = validateFile(request);

		String key = "prescriptions/%d/%s.%s".formatted(applicantId, UUID.randomUUID(), extension);
		r2StorageClient.upload(key, request.content(), normalizeContentType(request.contentType()));

		String previousKey = prescription.attachFile(key, normalizeContentType(request.contentType()),
				request.originalName());
		PrescriptionResponseDto response = toResponse(prescriptionRepository.save(prescription));

		// Limpiar el escaneo reemplazado no es crítico: si falla queda un huérfano en R2,
		// pero la receta ya apunta al archivo nuevo y el operador no tiene por qué enterarse.
		if (previousKey != null) {
			try {
				r2StorageClient.delete(previousKey);
			} catch (RuntimeException e) {
				log.warn("No se pudo borrar el archivo reemplazado {} de la receta {}", previousKey,
						prescriptionId, e);
			}
		}
		return response;
	}

	@Override
	public PrescriptionFileResponseDto getPrescriptionFile(Long applicantId, Long prescriptionId) {
		Prescription prescription = findPrescription(applicantId, prescriptionId);
		if (!prescription.hasFile()) {
			throw new PrescriptionFileNotFoundException();
		}
		PresignedUrl presigned = r2StorageClient.presignedGetUrl(prescription.getFileKey());
		return new PrescriptionFileResponseDto(presigned.url(), presigned.expiresAt(),
				prescription.getFileOriginalName(), prescription.getFileContentType());
	}

	/** Devuelve la extensión que le corresponde al tipo de archivo. */
	private String validateFile(PrescriptionFileUploadRequestDto request) {
		if (request.content() == null || request.content().length == 0) {
			throw new InvalidPrescriptionFileException("El archivo está vacío");
		}
		String extension = ALLOWED_CONTENT_TYPES.get(normalizeContentType(request.contentType()));
		if (extension == null) {
			throw new InvalidPrescriptionFileException("El archivo debe ser PDF, JPG o PNG");
		}
		return extension;
	}

	// El navegador puede mandar parámetros ("image/jpeg; charset=..."): queda solo el tipo.
	private String normalizeContentType(String contentType) {
		if (contentType == null) {
			return "";
		}
		return contentType.split(";")[0].trim().toLowerCase();
	}

	private Applicant findApplicant(Long applicantId) {
		return applicantRepository.findById(applicantId).orElseThrow(ApplicantNotFoundException::new);
	}

	private Prescription findPrescription(Long applicantId, Long prescriptionId) {
		findApplicant(applicantId);
		return prescriptionRepository.findByIdAndApplicantId(prescriptionId, applicantId)
				.orElseThrow(PrescriptionNotFoundException::new);
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
				prescription.getFileOriginalName(), prescription.getCreatedAt());
	}
}
