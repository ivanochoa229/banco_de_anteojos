package com.banco.anteojos.backend.orchestrator.indicators;

import java.time.LocalDate;

import com.banco.anteojos.backend.business.indicators.dto.response.ImpactIndicatorsResponseDto;

public interface IndicatorsUseCaseOrchestrator {

	/** Sin from/to, el panel muestra los últimos 12 meses. */
	ImpactIndicatorsResponseDto getImpactIndicators(LocalDate from, LocalDate to);
}
