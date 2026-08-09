package com.banco.anteojos.backend.thirdPartyServiceComunication.notifications;

import java.time.LocalDateTime;

/**
 * Notificaciones a beneficiarios (RF-22). Canal actual: email. Enviar es una operación no
 * crítica: cualquier implementación tiene que tragarse sus fallos (log y seguir), nunca
 * romper el flujo que la disparó.
 */
public interface NotificationClient {

	void sendAppointmentScheduled(String email, String firstName, LocalDateTime scheduledAt);

	void sendAppointmentRescheduled(String email, String firstName, LocalDateTime scheduledAt);

	void sendAppointmentCancelled(String email, String firstName, LocalDateTime scheduledAt);
}
