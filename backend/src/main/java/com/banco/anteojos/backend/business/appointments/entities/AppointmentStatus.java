package com.banco.anteojos.backend.business.appointments.entities;

/**
 * Todo turno nace PENDING_PAYMENT: pasa a PENDING_REVIEW cuando el beneficiario sube el
 * comprobante de la transferencia del bono contribución (evita que agenden turnos sin intención
 * de venir), y recién a SCHEDULED cuando el administrativo revisa ese comprobante y lo aprueba.
 * Si la revisión encuentra un problema, se cancela desde PENDING_REVIEW igual que se cancelaría
 * un turno ya agendado. De SCHEDULED se reprograma (sigue SCHEDULED), se cancela o se registra la
 * asistencia del día del turno (COMPLETED si vino, MISSED si faltó). Los tres estados finales
 * quedan para los indicadores de RF-26/27.
 */
public enum AppointmentStatus {
	PENDING_PAYMENT,
	PENDING_REVIEW,
	SCHEDULED,
	COMPLETED,
	MISSED,
	CANCELLED
}
