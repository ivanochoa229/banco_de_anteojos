package com.banco.anteojos.backend.business.appointments.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * {@code bookedCount} son las franjas tomadas por turnos no cancelados; {@code availableCount} las
 * libres que todavía no pasaron (en el mismo día no cuenta una hora ya vencida).
 */
public record AppointmentDayResponseDto(
		Long id,
		LocalDate date,
		LocalTime startTime,
		LocalTime endTime,
		int slotDurationMinutes,
		int slotCount,
		int bookedCount,
		int availableCount) {
}
