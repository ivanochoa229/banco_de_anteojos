package com.banco.anteojos.backend.business.indicators;

import java.time.LocalDate;
import java.util.List;

import com.banco.anteojos.backend.business.assignments.dto.response.MonthlyDeliveryResponseDto;
import com.banco.anteojos.backend.business.indicators.dto.response.ImpactIndicatorsResponseDto;

public interface IndicatorsService {

	/** Arma el panel con los conteos que el orchestrator ya resolvió en cada dominio (RF-26/27). */
	ImpactIndicatorsResponseDto build(LocalDate from, LocalDate to, long framesReceived,
			long assignmentsDelivered, long applicantsServed, long appointmentsAttended,
			long appointmentsMissed, long shipmentsDelivered, List<MonthlyDeliveryResponseDto> byMonth);
}
