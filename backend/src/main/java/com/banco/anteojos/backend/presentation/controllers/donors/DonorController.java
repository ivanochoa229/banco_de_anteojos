package com.banco.anteojos.backend.presentation.controllers.donors;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.donors.dto.request.DonorCreationRequestDto;
import com.banco.anteojos.backend.business.donors.dto.response.DonorResponseDto;
import com.banco.anteojos.backend.orchestrator.donors.DonorUseCaseOrchestrator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/donors")
@RequiredArgsConstructor
public class DonorController {

	private final DonorUseCaseOrchestrator donorOrchestrator;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public DonorResponseDto create(@Valid @RequestBody DonorCreationRequestDto request) {
		return donorOrchestrator.createDonor(request);
	}

	@GetMapping
	public List<DonorResponseDto> list() {
		return donorOrchestrator.listDonors();
	}

	@GetMapping("/{donorId}")
	public DonorResponseDto get(@PathVariable Long donorId) {
		return donorOrchestrator.getDonor(donorId);
	}
}
