package com.banco.anteojos.backend.thirdPartyServiceComunication.notifications;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
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

	@Override
	public void sendFrameDelivered(String email, String donorName, String sealCode, EmailAttachment photo) {
		String photoNote = photo == null ? "" : """

				Te adjuntamos una foto de la entrega, compartida con la autorización de quien \
				recibió los anteojos.
				""";
		send(email, "Tu marco fue entregado — Banco de Anteojos", """
				Hola %s:

				Queremos contarte que el marco que donaste al Banco de Anteojos (Fundación Hacer \
				Futuro), precinto %s, ya fue entregado con sus cristales graduados a una persona \
				que los necesitaba.
				%s
				¡Muchas gracias por tu donación!
				""".formatted(donorName, sealCode, photoNote), photo);
	}

	private void send(String email, String subject, String body) {
		send(email, subject, body, null);
	}

	/**
	 * Best effort: sin email cargado o sin SMTP configurado se saltea, y un fallo del envío se
	 * loguea y no corta el caso de uso que lo disparó (el turno o la entrega ya quedaron guardados).
	 */
	private void send(String email, String subject, String body, EmailAttachment attachment) {
		if (email == null || email.isBlank()) {
			log.debug("Notificación '{}' salteada: el destinatario no tiene email", subject);
			return;
		}
		if (host.isBlank()) {
			log.debug("Notificación '{}' salteada: SMTP sin configurar (MAIL_HOST)", subject);
			return;
		}
		try {
			if (attachment == null) {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setFrom(from);
				message.setTo(email);
				message.setSubject(subject);
				message.setText(body);
				mailSender.send(message);
				return;
			}
			// Con adjunto hace falta un mensaje MIME multipart; el texto plano no alcanza.
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(from);
			helper.setTo(email);
			helper.setSubject(subject);
			helper.setText(body);
			helper.addAttachment(attachment.fileName(), new ByteArrayResource(attachment.content()),
					attachment.contentType());
			mailSender.send(message);
		} catch (Exception e) {
			log.warn("No se pudo enviar la notificación '{}' a {}", subject, email, e);
		}
	}
}
