package com.banco.anteojos.backend.business.appointments;

import java.util.List;

import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentDayRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentDayResponseDto;

/** Configuración de los días en que la sede atiende (RF-20), a cargo del personal. */
public interface AppointmentDayService {

	/** Días de hoy en adelante, ordenados por fecha: los pasados ya no se configuran. */
	List<AppointmentDayResponseDto> listUpcomingDays();

	/** Lo que ve el beneficiario al pedir turno: días por venir con al menos una franja libre. */
	List<AppointmentDayResponseDto> listBookableDays();

	AppointmentDayResponseDto createDay(AppointmentDayRequestDto request);

	/** Bloquea el día mientras lo edita, igual que la reserva. Requiere transacción abierta. */
	AppointmentDayResponseDto updateDay(Long appointmentDayId, AppointmentDayRequestDto request);

	/** Solo si no tiene turnos activos. Requiere transacción abierta. */
	void deleteDay(Long appointmentDayId);
}
