package com.banco.anteojos.backend.business.appointments.entities;

/**
 * Todo turno nace PENDING_PAYMENT: recién pasa a SCHEDULED cuando el beneficiario sube el
 * comprobante de la transferencia del bono contribución (evita que agenden turnos sin intención
 * de venir). De SCHEDULED se reprograma (sigue SCHEDULED), se cancela o se registra la asistencia
 * del día del turno (COMPLETED si vino, MISSED si faltó). Los tres estados finales quedan para los
 * indicadores de RF-26/27.
 */
public enum AppointmentStatus {
	PENDING_PAYMENT,
	SCHEDULED,
	COMPLETED,
	MISSED,
	CANCELLED
}
