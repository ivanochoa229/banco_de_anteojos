package com.banco.anteojos.backend.useCase.indicators.unit.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.banco.anteojos.backend.business.assignments.dto.response.MonthlyDeliveryResponseDto;
import com.banco.anteojos.backend.business.indicators.IndicatorsServiceHandler;
import com.banco.anteojos.backend.business.indicators.dto.response.ImpactIndicatorsResponseDto;

@Tag("unit")
class IndicatorsServiceHandlerTest {

	private final IndicatorsServiceHandler indicatorsServiceHandler = new IndicatorsServiceHandler();

	@Test
	void Build_ComputesAttendanceRate() {
		LocalDate from = LocalDate.of(2026, 1, 1);
		LocalDate to = LocalDate.of(2026, 2, 28);
		List<MonthlyDeliveryResponseDto> byMonth = List.of(
				new MonthlyDeliveryResponseDto(YearMonth.of(2026, 1), 3, 3));

		ImpactIndicatorsResponseDto response = indicatorsServiceHandler.build(from, to, 5, 3, 3, 8, 2, 1,
				byMonth);

		assertThat(response.from()).isEqualTo(from);
		assertThat(response.to()).isEqualTo(to);
		assertThat(response.framesReceived()).isEqualTo(5);
		assertThat(response.assignmentsDelivered()).isEqualTo(3);
		assertThat(response.applicantsServed()).isEqualTo(3);
		assertThat(response.appointmentsAttended()).isEqualTo(8);
		assertThat(response.appointmentsMissed()).isEqualTo(2);
		// 8 asistidos de 10 turnos cerrados (8 + 2).
		assertThat(response.appointmentsAttendanceRate()).isEqualTo(0.8);
		assertThat(response.shipmentsDelivered()).isEqualTo(1);
		assertThat(response.byMonth()).isEqualTo(byMonth);
	}

	@Test
	void Build_WhenNoAppointmentsClosedRateIsNull() {
		ImpactIndicatorsResponseDto response = indicatorsServiceHandler.build(LocalDate.now(),
				LocalDate.now(), 0, 0, 0, 0, 0, 0, List.of());

		// Sin turnos completados ni faltados no hay base para calcular un ratio.
		assertThat(response.appointmentsAttendanceRate()).isNull();
	}
}
