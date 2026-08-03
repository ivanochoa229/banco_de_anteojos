package com.banco.anteojos.backend.presentation.controllers.applicants;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.banco.anteojos.backend.business.applicants.dto.request.AnsesCertificateUploadRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.AnsesCertificateResponseDto;
import com.banco.anteojos.backend.business.applicants.exception.InvalidAnsesCertificateException;
import com.banco.anteojos.backend.orchestrator.applicants.ApplicantUseCaseOrchestrator;

import lombok.RequiredArgsConstructor;

// Sub-recurso singleton: un solicitante tiene a lo sumo una negativa vigente.
// PUT y no POST: subir otra la reemplaza (la anterior vence a los 30 días y no sirve).
@RestController
@RequestMapping("/v1/applicants/{applicantId}/anses-certificate")
@RequiredArgsConstructor
public class ApplicantAnsesCertificateController {

	private final ApplicantUseCaseOrchestrator applicantOrchestrator;

	@PutMapping
	public AnsesCertificateResponseDto upload(@PathVariable Long applicantId,
			@RequestParam("file") MultipartFile file) {
		return applicantOrchestrator.uploadAnsesCertificate(applicantId, toUploadRequest(file));
	}

	@GetMapping
	public AnsesCertificateResponseDto get(@PathVariable Long applicantId) {
		return applicantOrchestrator.getAnsesCertificate(applicantId);
	}

	private AnsesCertificateUploadRequestDto toUploadRequest(MultipartFile file) {
		try {
			return new AnsesCertificateUploadRequestDto(file.getBytes(), file.getContentType(),
					file.getOriginalFilename());
		} catch (IOException e) {
			throw new InvalidAnsesCertificateException("No se pudo leer el archivo");
		}
	}
}
