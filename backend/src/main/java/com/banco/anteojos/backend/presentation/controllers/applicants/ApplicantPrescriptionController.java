package com.banco.anteojos.backend.presentation.controllers.applicants;

import java.io.IOException;
import java.util.List;

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
import org.springframework.web.multipart.MultipartFile;

import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionCreationRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionFileUploadRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionFileResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionResponseDto;
import com.banco.anteojos.backend.business.applicants.exception.InvalidPrescriptionFileException;
import com.banco.anteojos.backend.orchestrator.applicants.ApplicantUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/applicants/{applicantId}/prescriptions")
@RequiredArgsConstructor
public class ApplicantPrescriptionController {

	private final ApplicantUseCaseOrchestrator applicantOrchestrator;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PrescriptionResponseDto create(@PathVariable Long applicantId,
			@Valid @RequestBody PrescriptionCreationRequestDto request) {
		return applicantOrchestrator.addPrescription(applicantId, request);
	}

	@GetMapping
	public List<PrescriptionResponseDto> list(@PathVariable Long applicantId) {
		return applicantOrchestrator.listPrescriptions(applicantId);
	}

	// PUT y no POST: subir de nuevo reemplaza el archivo de la receta, no agrega otro.
	@PutMapping("/{prescriptionId}/file")
	public PrescriptionResponseDto uploadFile(@PathVariable Long applicantId,
			@PathVariable Long prescriptionId, @RequestParam("file") MultipartFile file) {
		return applicantOrchestrator.uploadPrescriptionFile(applicantId, prescriptionId,
				toUploadRequest(file));
	}

	@GetMapping("/{prescriptionId}/file")
	public PrescriptionFileResponseDto getFile(@PathVariable Long applicantId,
			@PathVariable Long prescriptionId) {
		return applicantOrchestrator.getPrescriptionFile(applicantId, prescriptionId);
	}

	private PrescriptionFileUploadRequestDto toUploadRequest(MultipartFile file) {
		try {
			return new PrescriptionFileUploadRequestDto(file.getBytes(), file.getContentType(),
					file.getOriginalFilename());
		} catch (IOException e) {
			throw new InvalidPrescriptionFileException("No se pudo leer el archivo");
		}
	}
}
