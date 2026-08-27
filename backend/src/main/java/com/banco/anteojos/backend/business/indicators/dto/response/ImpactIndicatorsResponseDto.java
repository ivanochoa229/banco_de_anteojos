package com.banco.anteojos.backend.business.indicators.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.banco.anteojos.backend.business.assignments.dto.response.MonthlyDeliveryResponseDto;

public record ImpactIndicatorsResponseDto(
		LocalDate from,
		LocalDate to,
		long framesReceived,
		long assignmentsDelivered,
		long applicantsServed,
		long appointmentsAttended,
		long appointmentsMissed,
		/** Null si no hubo turnos en un estado final dentro del rango: no hay ratio que calcular. */
		Double appointmentsAttendanceRate,
		long shipmentsDelivered,
		List<MonthlyDeliveryResponseDto> byMonth) {
}
