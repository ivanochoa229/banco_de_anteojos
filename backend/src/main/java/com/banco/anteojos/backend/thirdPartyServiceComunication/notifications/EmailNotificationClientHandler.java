package com.banco.anteojos.backend.thirdPartyServiceComunication.notifications;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EmailNotificationClientHandler implements NotificationClient {

	private static final DateTimeFormatter DATE_FORMAT =
			DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm 'hs'");

	private final JavaMailSender mailSender;
	private final String from;
	private final String host;

	public EmailNotificationClientHandler(JavaMailSender mailSender,
			@Value("${notifications.mail.from:}") String from,
			@Value("${spring.mail.host:}") String host) {
		this.mailSender = mailSender;
		this.from = from;
		this.host = host;
	}

	@Override
	public void sendAppointmentScheduled(String email, String firstName, LocalDateTime scheduledAt) {
		send(email, "Turno confirmado — Banco de Anteojos", """
				Hola %s:

				Te confirmamos tu turno en el Banco de Anteojos (Fundación Hacer Futuro) \
				para el %s.

				Dirección: Chacabuco 27, San Miguel de Tucumán.

				Si no podés asistir, comunicate con la fundación para reprogramarlo.
				""".formatted(firstName, DATE_FORMAT.format(scheduledAt)));
	}

	@Override
	public void sendAppointmentRescheduled(String email, String firstName, LocalDateTime scheduledAt) {
		send(email, "Turno reprogramado — Banco de Anteojos", """
				Hola %s:

				Tu turno en el Banco de Anteojos (Fundación Hacer Futuro) fue reprogramado \
				para el %s.

				Dirección: Chacabuco 27, San Miguel de Tucumán.

				Si no podés asistir, comunicate con la fundación para reprogramarlo.
				""".formatted(firstName, DATE_FORMAT.format(scheduledAt)));
	}

	@Override
	public void sendAppointmentCancelled(String email, String firstName, LocalDateTime scheduledAt) {
		send(email, "Turno cancelado — Banco de Anteojos", """
				Hola %s:

				Tu turno del %s en el Banco de Anteojos (Fundación Hacer Futuro) fue cancelado.

				Comunicate con la fundación si querés agendar uno nuevo.
				""".formatted(firstName, DATE_FORMAT.format(scheduledAt)));
	}

	/**
	 * Best effort: sin email cargado o sin SMTP configurado se saltea, y un fallo del envío se
	 * loguea y no corta el caso de uso que lo disparó (el turno ya quedó guardado).
	 */
	private void send(String email, String subject, String body) {
		if (email == null || email.isBlank()) {
			log.debug("Notificación '{}' salteada: el beneficiario no tiene email", subject);
			return;
		}
		if (host.isBlank()) {
			log.debug("Notificación '{}' salteada: SMTP sin configurar (MAIL_HOST)", subject);
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(email);
			message.setSubject(subject);
			message.setText(body);
			mailSender.send(message);
		} catch (Exception e) {
			log.warn("No se pudo enviar la notificación '{}' a {}", subject, email, e);
		}
	}
}
