package com.banco.anteojos.backend.business.appointments.entities;

/**
 * SCHEDULED es el único estado vivo: de ahí se reprograma (sigue SCHEDULED), se cancela o se
 * registra la asistencia del día del turno (COMPLETED si vino, MISSED si faltó). Los tres estados
 * finales quedan para los indicadores de RF-26/27.
 */
public enum AppointmentStatus {
	SCHEDULED,
	COMPLETED,
	MISSED,
	CANCELLED
}
