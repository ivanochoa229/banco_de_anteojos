package com.banco.anteojos.backend.useCase.notifications.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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

import com.banco.anteojos.backend.thirdPartyServiceComunication.notifications.EmailNotificationClientHandler;

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
