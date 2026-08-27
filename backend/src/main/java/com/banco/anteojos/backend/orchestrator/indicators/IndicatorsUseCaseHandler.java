package com.banco.anteojos.backend.orchestrator.indicators;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.banco.anteojos.backend.business.appointments.AppointmentService;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus;
import com.banco.anteojos.backend.business.assignments.AssignmentService;
import com.banco.anteojos.backend.business.assignments.dto.response.MonthlyDeliveryResponseDto;
import com.banco.anteojos.backend.business.frames.FrameService;
import com.banco.anteojos.backend.business.indicators.IndicatorsService;
import com.banco.anteojos.backend.business.indicators.dto.response.ImpactIndicatorsResponseDto;
import com.banco.anteojos.backend.business.indicators.exception.InvalidIndicatorsRangeException;
import com.banco.anteojos.backend.business.shipments.ShipmentService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IndicatorsUseCaseHandler implements IndicatorsUseCaseOrchestrator {

	private static final int DEFAULT_RANGE_MONTHS = 12;

	private final FrameService frameService;
	private final AssignmentService assignmentService;
	private final AppointmentService appointmentService;
	private final ShipmentService shipmentService;
	private final IndicatorsService indicatorsService;

	@Override
	public ImpactIndicatorsResponseDto getImpactIndicators(LocalDate from, LocalDate to) {
		LocalDate resolvedTo = to != null ? to : LocalDate.now();
		LocalDate resolvedFrom = from != null ? from : resolvedTo.minusMonths(DEFAULT_RANGE_MONTHS);
		if (resolvedFrom.isAfter(resolvedTo)) {
			throw new InvalidIndicatorsRangeException("La fecha desde no puede ser posterior a la fecha hasta");
		}

		// [inicio del día "desde", inicio del día siguiente a "hasta"): el rango es inclusive
		// en ambas puntas para quien lo pide por fecha de calendario.
		LocalDateTime rangeStart = resolvedFrom.atStartOfDay();
		LocalDateTime rangeEnd = resolvedTo.plusDays(1).atStartOfDay();

		long framesReceived = frameService.countReceived(rangeStart, rangeEnd);
		long assignmentsDelivered = assignmentService.countDelivered(rangeStart, rangeEnd);
		long applicantsServed = assignmentService.countDistinctApplicantsServed(rangeStart, rangeEnd);
		long appointmentsAttended = appointmentService.countByStatus(AppointmentStatus.COMPLETED, rangeStart,
				rangeEnd);
		long appointmentsMissed = appointmentService.countByStatus(AppointmentStatus.MISSED, rangeStart,
				rangeEnd);
		long shipmentsDelivered = shipmentService.countDelivered(rangeStart, rangeEnd);
		List<MonthlyDeliveryResponseDto> byMonth = assignmentService.deliveriesByMonth(rangeStart, rangeEnd);

		return indicatorsService.build(resolvedFrom, resolvedTo, framesReceived, assignmentsDelivered,
				applicantsServed, appointmentsAttended, appointmentsMissed, shipmentsDelivered, byMonth);
	}
}
