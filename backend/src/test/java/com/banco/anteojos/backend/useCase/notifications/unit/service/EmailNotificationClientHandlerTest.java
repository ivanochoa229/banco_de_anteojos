package com.banco.anteojos.backend.useCase.notifications.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.banco.anteojos.backend.thirdPartyServiceComunication.notifications.EmailAttachment;
import com.banco.anteojos.backend.thirdPartyServiceComunication.notifications.EmailNotificationClientHandler;

import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class EmailNotificationClientHandlerTest {

	private static final LocalDateTime SCHEDULED_AT = LocalDateTime.of(2026, 8, 20, 10, 30);

	@Mock
	private JavaMailSender mailSender;

	private EmailNotificationClientHandler handler() {
		return new EmailNotificationClientHandler(mailSender, "banco@fundacion.org", "smtp.gmail.com");
	}

	@Test
	void SendAppointmentScheduled_Successful() {
		handler().sendAppointmentScheduled("juana.perez@mail.com", "Juana", SCHEDULED_AT);

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());
		SimpleMailMessage message = captor.getValue();
		assertThat(message.getTo()).containsExactly("juana.perez@mail.com");
		assertThat(message.getFrom()).isEqualTo("banco@fundacion.org");
		assertThat(message.getSubject()).contains("Turno confirmado");
		assertThat(message.getText()).contains("Juana").contains("20/08/2026 a las 10:30 hs");
	}

	@Test
	void SendAppointmentCancelled_Successful() {
		handler().sendAppointmentCancelled("juana.perez@mail.com", "Juana", SCHEDULED_AT);

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());
		assertThat(captor.getValue().getSubject()).contains("Turno cancelado");
	}

	@Test
	void SendFrameDelivered_WithoutPhoto() {
		handler().sendFrameDelivered("contacto@rotary.org", "Rotary Club Tucumán", "A-1001", null);

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());
		SimpleMailMessage message = captor.getValue();
		assertThat(message.getTo()).containsExactly("contacto@rotary.org");
		assertThat(message.getSubject()).contains("Tu marco fue entregado");
		assertThat(message.getText()).contains("Rotary Club Tucumán").contains("A-1001")
				.doesNotContain("adjuntamos");
	}

	@Test
	void SendFrameDelivered_WithPhoto() throws MessagingException, IOException {
		when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

		handler().sendFrameDelivered("contacto@rotary.org", "Rotary Club Tucumán", "A-1001",
				new EmailAttachment(new byte[] { 1, 2, 3 }, "image/jpeg", "entrega.jpg"));

		ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
		verify(mailSender).send(captor.capture());
		MimeMessage message = captor.getValue();
		assertThat(message.getAllRecipients()[0].toString()).isEqualTo("contacto@rotary.org");
		assertThat(message.getSubject()).contains("Tu marco fue entregado");

		Multipart content = (Multipart) message.getContent();
		Part attachment = null;
		for (int i = 0; i < content.getCount(); i++) {
			if (Part.ATTACHMENT.equalsIgnoreCase(content.getBodyPart(i).getDisposition())) {
				attachment = content.getBodyPart(i);
			}
		}
		assertThat(attachment).isNotNull();
		assertThat(attachment.getFileName()).isEqualTo("entrega.jpg");
	}

	@Test
	void Send_WhenApplicantHasNoEmail() {
		// Sin email cargado no hay a quién avisar: se saltea sin ruido.
		handler().sendAppointmentScheduled(null, "Juana", SCHEDULED_AT);
		handler().sendAppointmentRescheduled("   ", "Juana", SCHEDULED_AT);

		verifyNoInteractions(mailSender);
	}

	@Test
	void Send_WhenSmtpIsNotConfigured() {
		new EmailNotificationClientHandler(mailSender, "", "")
				.sendAppointmentScheduled("juana.perez@mail.com", "Juana", SCHEDULED_AT);

		verifyNoInteractions(mailSender);
	}

	@Test
	void Send_WhenMailServerFails() {
		doThrow(new MailSendException("SMTP caído")).when(mailSender).send(any(SimpleMailMessage.class));

		// Notificar es best effort: el fallo se loguea y no llega al caso de uso.
		assertThatCode(() -> handler().sendAppointmentScheduled("juana.perez@mail.com", "Juana", SCHEDULED_AT))
				.doesNotThrowAnyException();
	}
}
