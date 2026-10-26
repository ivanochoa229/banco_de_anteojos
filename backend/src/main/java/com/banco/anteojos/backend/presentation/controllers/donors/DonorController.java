package com.banco.anteojos.backend.presentation.controllers.donors;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.business.donors.dto.request.DonorCreationRequestDto;
import com.banco.anteojos.backend.business.donors.dto.response.DonorResponseDto;
import com.banco.anteojos.backend.orchestrator.donors.DonorUseCaseOrchestrator;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
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

	/**
	 * {@code ?q=} busca por nombre. {@code ?ids=1,2,3} trae esos donantes: el inventario lo usa para
	 * mostrar de quién vino cada marco, porque el listado de marcos (que también ve el solicitante
	 * desde el probador) no lleva el nombre del donante.
	 */
	@GetMapping
	public PageResponseDto<DonorResponseDto> list(@RequestParam(defaultValue = "") String q,
			@RequestParam(required = false) List<Long> ids,
			@RequestParam(defaultValue = "0") @PositiveOrZero int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return donorOrchestrator.listDonors(q, ids, PageRequest.of(page, size));
	}

	@GetMapping("/{donorId}")
	public DonorResponseDto get(@PathVariable Long donorId) {
		return donorOrchestrator.getDonor(donorId);
	}
}
