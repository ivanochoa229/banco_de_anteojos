package com.banco.anteojos.backend.thirdPartyServiceComunication.notifications;

import java.time.LocalDateTime;

/**
 * Notificaciones a beneficiarios (RF-22) y a donantes. Canal actual: email. Enviar es una
 * operación no crítica: cualquier implementación tiene que tragarse sus fallos (log y seguir),
 * nunca romper el flujo que la disparó.
 */
public interface NotificationClient {

	void sendAppointmentScheduled(String email, String firstName, LocalDateTime scheduledAt);

	void sendAppointmentRescheduled(String email, String firstName, LocalDateTime scheduledAt);

	void sendAppointmentCancelled(String email, String firstName, LocalDateTime scheduledAt);

	/**
	 * Le avisa al donante que el marco que donó llegó a destino.
	 *
	 * @param photo foto de la entrega para adjuntar, o null para mandar el aviso sin foto.
	 */
	void sendFrameDelivered(String email, String donorName, String sealCode, EmailAttachment photo);
}
