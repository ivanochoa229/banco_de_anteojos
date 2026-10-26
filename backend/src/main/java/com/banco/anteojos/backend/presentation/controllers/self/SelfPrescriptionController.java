package com.banco.anteojos.backend.presentation.controllers.self;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.banco.anteojos.backend.business.applicants.dto.request.PrescriptionFileUploadRequestDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionFileResponseDto;
import com.banco.anteojos.backend.business.applicants.dto.response.PrescriptionResponseDto;
import com.banco.anteojos.backend.business.applicants.exception.InvalidPrescriptionFileException;
import com.banco.anteojos.backend.business.security.CurrentUserService;
import com.banco.anteojos.backend.orchestrator.applicants.ApplicantUseCaseOrchestrator;

import lombok.RequiredArgsConstructor;

// El beneficiario sube la receta del médico tal cual (PDF o foto): la graduación la lee la óptica
// del archivo y el operador la tipea si la necesita. El applicantId sale del JWT, y leer el archivo
// de una receta ajena da 404 porque se busca por id y solicitante a la vez.
@RestController
@RequestMapping("/v1/me/prescriptions")
@RequiredArgsConstructor
public class SelfPrescriptionController {

	private final ApplicantUseCaseOrchestrator applicantOrchestrator;
	private final CurrentUserService currentUserService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PrescriptionResponseDto create(@RequestParam("file") MultipartFile file) {
		return applicantOrchestrator.addPrescriptionFromFile(currentUserService.getApplicantId(),
				toUploadRequest(file));
	}

	@GetMapping
	public List<PrescriptionResponseDto> list() {
		return applicantOrchestrator.listPrescriptions(currentUserService.getApplicantId());
	}

	@GetMapping("/{prescriptionId}/file")
	public PrescriptionFileResponseDto getFile(@PathVariable Long prescriptionId) {
		return applicantOrchestrator.getPrescriptionFile(currentUserService.getApplicantId(), prescriptionId);
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
