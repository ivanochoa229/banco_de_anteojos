package com.banco.anteojos.backend.presentation.controllers.self;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.banco.anteojos.backend.business.applicants.dto.request.AnsesCertificateUploadRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.AnsesCertificateResponseDto;
import com.banco.anteojos.backend.business.applicants.exception.InvalidAnsesCertificateException;
import com.banco.anteojos.backend.business.security.CurrentUserService;
import com.banco.anteojos.backend.orchestrator.applicants.ApplicantUseCaseOrchestrator;

import lombok.RequiredArgsConstructor;

// El beneficiario sube su propia negativa y el sistema la valida igual que cuando la carga el
// operador (CUIL contra el DNI + vigencia ≤ 30 días). El applicantId sale del JWT.
@RestController
@RequestMapping("/v1/me/anses-certificate")
@RequiredArgsConstructor
public class SelfAnsesCertificateController {

	private final ApplicantUseCaseOrchestrator applicantOrchestrator;
	private final CurrentUserService currentUserService;

	@PutMapping
	public AnsesCertificateResponseDto upload(@RequestParam("file") MultipartFile file) {
		return applicantOrchestrator.uploadAnsesCertificate(currentUserService.getApplicantId(),
				toUploadRequest(file));
	}

	@GetMapping
	public AnsesCertificateResponseDto get() {
		return applicantOrchestrator.getAnsesCertificate(currentUserService.getApplicantId());
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
