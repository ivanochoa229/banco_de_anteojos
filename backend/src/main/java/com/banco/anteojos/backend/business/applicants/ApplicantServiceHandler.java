package com.banco.anteojos.backend.business.applicants;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.applicants.dto.request.AnsesCertificateUploadRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.ApplicantUpdateRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionFileUploadRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.AnsesCertificateResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionFileResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionResponseDto;
import com.banco.anteojos.backend.business.applicants.entities.AnsesCertificate;
import com.banco.anteojos.backend.business.applicants.entities.Applicant;
import com.banco.anteojos.backend.business.applicants.entities.Prescription;
import com.banco.anteojos.backend.business.applicants.exception.AnsesCertificateNotFoundException;
import com.banco.anteojos.backend.business.applicants.exception.ApplicantNotFoundException;
import com.banco.anteojos.backend.business.applicants.exception.DniAlreadyExistsException;
import com.banco.anteojos.backend.business.applicants.exception.InvalidAnsesCertificateException;
import com.banco.anteojos.backend.business.applicants.exception.InvalidPrescriptionFileException;
import com.banco.anteojos.backend.business.applicants.exception.PrescriptionFileNotFoundException;
import com.banco.anteojos.backend.business.applicants.exception.PrescriptionNotFoundException;
import com.banco.anteojos.backend.persistence.applicants.AnsesCertificatePostgresSqlRepository;
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

	// Los datos son válidos por 30 días desde la emisión, según el propio PDF de ANSES (RF-05).
	private static final int ANSES_VALIDITY_DAYS = 30;

	// Pesos del dígito verificador del CUIL (algoritmo módulo 11 de AFIP).
	private static final int[] CUIL_WEIGHTS = { 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 };

	private final ApplicantPostgresSqlRepository applicantRepository;
	private final PrescriptionPostgresSqlRepository prescriptionRepository;
	private final AnsesCertificatePostgresSqlRepository ansesCertificateRepository;
	private final AnsesCertificateHelper ansesCertificateHelper;
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

	@Override
	public AnsesCertificateResponseDto uploadAnsesCertificate(Long applicantId,
			AnsesCertificateUploadRequestDto request) {
		Applicant applicant = findApplicant(applicantId);
		validateAnsesFile(request);

		ParsedAnsesCertificate parsed = ansesCertificateHelper.parse(request.content());
		validateAgainstApplicant(parsed, applicant);
		validateStillInForce(parsed.issueDate());

		String key = "anses/%d/%s.pdf".formatted(applicantId, UUID.randomUUID());
		r2StorageClient.upload(key, request.content(), "application/pdf");

		String previousKey = null;
		AnsesCertificate certificate = ansesCertificateRepository.findByApplicantId(applicantId).orElse(null);
		if (certificate == null) {
			certificate = new AnsesCertificate(applicantId, parsed.cuil(), parsed.transactionNumber(),
					parsed.issueDate(), key, "application/pdf", request.originalName());
		} else {
			previousKey = certificate.replaceWith(parsed.cuil(), parsed.transactionNumber(),
					parsed.issueDate(), key, "application/pdf", request.originalName());
		}
		certificate = ansesCertificateRepository.save(certificate);

		// La negativa validada confirma el CUIL del solicitante, que no se carga a mano.
		applicant.registerCuil(parsed.cuil());
		applicantRepository.save(applicant);

		// Borrar el PDF reemplazado no es crítico: si falla queda un huérfano en R2,
		// pero el certificado ya apunta al archivo nuevo.
		if (previousKey != null) {
			try {
				r2StorageClient.delete(previousKey);
			} catch (RuntimeException e) {
				log.warn("No se pudo borrar la negativa reemplazada {} del solicitante {}", previousKey,
						applicantId, e);
			}
		}
		return toResponse(certificate);
	}

	@Override
	public AnsesCertificateResponseDto getAnsesCertificate(Long applicantId) {
		findApplicant(applicantId);
		return toResponse(ansesCertificateRepository.findByApplicantId(applicantId)
				.orElseThrow(AnsesCertificateNotFoundException::new));
	}

	private void validateAnsesFile(AnsesCertificateUploadRequestDto request) {
		if (request.content() == null || request.content().length == 0) {
			throw new InvalidAnsesCertificateException("El archivo está vacío");
		}
		// Solo el PDF original descargado de ANSES: una foto o escaneo no garantiza que el
		// barcode y el texto sean legibles.
		if (!"application/pdf".equals(normalizeContentType(request.contentType()))) {
			throw new InvalidAnsesCertificateException("El archivo debe ser el PDF emitido por ANSES");
		}
	}

	private void validateAgainstApplicant(ParsedAnsesCertificate parsed, Applicant applicant) {
		if (!hasValidCheckDigit(parsed.cuil())) {
			throw new InvalidAnsesCertificateException("El CUIL de la certificación no es un CUIL válido");
		}
		// Los dígitos 3 a 10 del CUIL son el DNI (con ceros a la izquierda si tiene menos de 8).
		long dniInCuil = Long.parseLong(parsed.cuil().substring(2, 10));
		if (dniInCuil != Long.parseLong(applicant.getDni())) {
			throw new InvalidAnsesCertificateException(
					"La certificación pertenece a otra persona: el CUIL no corresponde al DNI del solicitante");
		}
	}

	private void validateStillInForce(LocalDate issueDate) {
		LocalDate today = LocalDate.now();
		if (issueDate.isAfter(today)) {
			throw new InvalidAnsesCertificateException("La fecha de emisión de la certificación es futura");
		}
		if (ChronoUnit.DAYS.between(issueDate, today) > ANSES_VALIDITY_DAYS) {
			throw new InvalidAnsesCertificateException(
					"La certificación está vencida: tiene más de 30 días desde su emisión");
		}
	}

	private boolean hasValidCheckDigit(String cuil) {
		int sum = 0;
		for (int i = 0; i < CUIL_WEIGHTS.length; i++) {
			sum += Character.getNumericValue(cuil.charAt(i)) * CUIL_WEIGHTS[i];
		}
		int expected = (11 - sum % 11) % 11;
		// Si el algoritmo da 10 no existe dígito verificador posible: ese CUIL no puede ser real.
		return expected != 10 && expected == Character.getNumericValue(cuil.charAt(10));
	}

	private AnsesCertificateResponseDto toResponse(AnsesCertificate certificate) {
		PresignedUrl presigned = r2StorageClient.presignedGetUrl(certificate.getFileKey());
		return new AnsesCertificateResponseDto(certificate.getId(), certificate.getApplicantId(),
				certificate.getCuil(), certificate.getTransactionNumber(), certificate.getIssueDate(),
				certificate.getFileOriginalName(), presigned.url(), presigned.expiresAt(),
				certificate.getCreatedAt());
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
