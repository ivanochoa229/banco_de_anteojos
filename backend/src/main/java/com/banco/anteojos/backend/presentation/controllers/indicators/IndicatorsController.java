package com.banco.anteojos.backend.presentation.controllers.indicators;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.banco.anteojos.backend.business.indicators.dto.response.ImpactIndicatorsResponseDto;
import com.banco.anteojos.backend.orchestrator.indicators.IndicatorsUseCaseOrchestrator;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/indicators")
@RequiredArgsConstructor
public class IndicatorsController {

	private final IndicatorsUseCaseOrchestrator indicatorsOrchestrator;

	/** Sin from/to, el panel de impacto muestra los últimos 12 meses (RF-26/27). */
	@GetMapping
	public ImpactIndicatorsResponseDto get(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return indicatorsOrchestrator.getImpactIndicators(from, to);
	}
}
