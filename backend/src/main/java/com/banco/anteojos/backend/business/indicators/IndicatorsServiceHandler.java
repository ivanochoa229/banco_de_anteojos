package com.banco.anteojos.backend.business.indicators;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.assignments.dto.response.MonthlyDeliveryResponseDto;
import com.banco.anteojos.backend.business.indicators.dto.response.ImpactIndicatorsResponseDto;

/**
 * Sin repositorio propio a propósito: no hay tabla {@code indicators}, el dominio solo agrega
 * datos que el orchestrator ya resolvió en frames/assignments/appointments/shipments.
 */
@Service
public class IndicatorsServiceHandler implements IndicatorsService {

	@Override
	public ImpactIndicatorsResponseDto build(LocalDate from, LocalDate to, long framesReceived,
			long assignmentsDelivered, long applicantsServed, long appointmentsAttended,
			long appointmentsMissed, long shipmentsDelivered, List<MonthlyDeliveryResponseDto> byMonth) {
		return new ImpactIndicatorsResponseDto(from, to, framesReceived, assignmentsDelivered,
				applicantsServed, appointmentsAttended, appointmentsMissed,
				attendanceRate(appointmentsAttended, appointmentsMissed), shipmentsDelivered, byMonth);
	}

	/** Null cuando no hubo turnos en un estado final: no hay base sobre la que calcular un ratio. */
	private Double attendanceRate(long attended, long missed) {
		long closed = attended + missed;
		return closed == 0 ? null : (double) attended / closed;
	}
}
