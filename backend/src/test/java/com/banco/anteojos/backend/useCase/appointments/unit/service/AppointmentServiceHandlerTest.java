package com.banco.anteojos.backend.useCase.appointments.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banco.anteojos.backend.business.appointments.AppointmentServiceHandler;
import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentCreationRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentReceiptUploadRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentReceiptResponseDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentResponseDto;
import com.banco.anteojos.backend.business.appointments.entities.Appointment;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus;
import com.banco.anteojos.backend.business.appointments.exception.AppointmentNotFoundException;
import com.banco.anteojos.backend.business.appointments.exception.AppointmentReceiptNotFoundException;
import com.banco.anteojos.backend.business.appointments.exception.InvalidAppointmentReceiptException;
import com.banco.anteojos.backend.business.appointments.exception.InvalidAppointmentTransitionException;
import com.banco.anteojos.backend.persistence.appointments.AppointmentPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AppointmentServiceHandlerTest {

	private static final LocalDateTime SCHEDULED_AT = LocalDateTime.now().plusDays(3);

	@Mock
	private AppointmentPostgresSqlRepository appointmentRepository;

	@Mock
	private R2StorageClient r2StorageClient;

	@InjectMocks
	private AppointmentServiceHandler appointmentServiceHandler;

	private Appointment appointment() {
		return new Appointment(1L, null, SCHEDULED_AT, null);
	}

	private Appointment confirmedAppointment() {
		Appointment appointment = pendingReviewAppointment();
		appointment.approve();
		return appointment;
	}

	private Appointment pendingReviewAppointment() {
		Appointment appointment = appointment();
		appointment.submitReceiptForReview("appointments/1/comprobante.pdf", "application/pdf", "comprobante.pdf");
		return appointment;
	}

	private void mockFind(Appointment appointment) {
		when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
		when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	private byte[] receiptPdf() {
		return pdfWithText("Comprobante de pago - Bono contribución");
	}

	private byte[] pdfWithText(String text) {
		try (PDDocument document = new PDDocument()) {
			PDPage page = new PDPage();
			document.addPage(page);
			try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
				stream.beginText();
				stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				stream.newLineAtOffset(50, 700);
				stream.showText(text);
				stream.endText();
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			document.save(out);
			return out.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Test
	void CreateAppointment_Successful() {
		when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

		AppointmentResponseDto response = appointmentServiceHandler.createAppointment(1L,
				new AppointmentCreationRequestDto(SCHEDULED_AT, 9L, "retiro de anteojos"));

		assertThat(response.applicantId()).isEqualTo(1L);
		assertThat(response.assignmentId()).isEqualTo(9L);
		assertThat(response.scheduledAt()).isEqualTo(SCHEDULED_AT);
		// Nace sin confirmar: recién queda SCHEDULED con el comprobante.
		assertThat(response.status()).isEqualTo("PENDING_PAYMENT");
		assertThat(response.notes()).isEqualTo("retiro de anteojos");
		assertThat(response.createdAt()).isNotNull();
	}

	@Test
	void GetAppointment_WhenNotFound() {
		when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> appointmentServiceHandler.getAppointment(999L))
				.isInstanceOf(AppointmentNotFoundException.class);
	}

	@Test
	void SubmitReceiptForReview_Successful() {
		mockFind(appointment());
		byte[] content = receiptPdf();

		AppointmentResponseDto response = appointmentServiceHandler.submitReceiptForReview(10L,
				new AppointmentReceiptUploadRequestDto(content, "application/pdf", "comprobante.pdf"));

		assertThat(response.status()).isEqualTo("PENDING_REVIEW");
		assertThat(response.receiptOriginalName()).isEqualTo("comprobante.pdf");
		verify(r2StorageClient).upload(anyString(), any(byte[].class), any());
	}

	@Test
	void SubmitReceiptForReview_WhenFileIsEmpty() {
		when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment()));

		assertThatThrownBy(() -> appointmentServiceHandler.submitReceiptForReview(10L,
				new AppointmentReceiptUploadRequestDto(new byte[0], "application/pdf", "comprobante.pdf")))
				.isInstanceOf(InvalidAppointmentReceiptException.class);
	}

	@Test
	void SubmitReceiptForReview_WhenContentTypeNotAllowed() {
		when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment()));

		assertThatThrownBy(() -> appointmentServiceHandler.submitReceiptForReview(10L,
				new AppointmentReceiptUploadRequestDto(new byte[] { 1 }, "text/plain", "comprobante.txt")))
				.isInstanceOf(InvalidAppointmentReceiptException.class);
	}

	@Test
	void SubmitReceiptForReview_WhenAlreadySubmitted() {
		when(appointmentRepository.findById(10L)).thenReturn(Optional.of(pendingReviewAppointment()));

		assertThatThrownBy(() -> appointmentServiceHandler.submitReceiptForReview(10L,
				new AppointmentReceiptUploadRequestDto(receiptPdf(), "application/pdf", "otro.pdf")))
				.isInstanceOf(InvalidAppointmentTransitionException.class);
	}

	@Test
	void SubmitReceiptForReview_WhenContentDoesNotMatchDeclaredType() {
		when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment()));

		assertThatThrownBy(() -> appointmentServiceHandler.submitReceiptForReview(10L,
				new AppointmentReceiptUploadRequestDto(new byte[] { 1, 2, 3 }, "application/pdf", "falso.pdf")))
				.isInstanceOf(InvalidAppointmentReceiptException.class);
	}

	@Test
	void SubmitReceiptForReview_WhenPdfHasNoReceiptLikeText() {
		when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment()));

		assertThatThrownBy(() -> appointmentServiceHandler.submitReceiptForReview(10L,
				new AppointmentReceiptUploadRequestDto(pdfWithText("una foto de unos anteojos"),
						"application/pdf", "anteojos.pdf")))
				.isInstanceOf(InvalidAppointmentReceiptException.class);
	}

	@Test
	void Approve_Successful() {
		mockFind(pendingReviewAppointment());

		AppointmentResponseDto response = appointmentServiceHandler.approve(10L);

		assertThat(response.status()).isEqualTo("SCHEDULED");
	}

	@Test
	void Approve_WhenNotPendingReview() {
		when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment()));

		assertThatThrownBy(() -> appointmentServiceHandler.approve(10L))
				.isInstanceOf(InvalidAppointmentTransitionException.class);
	}

	@Test
	void Cancel_WhenPendingReview() {
		mockFind(pendingReviewAppointment());

		AppointmentResponseDto response = appointmentServiceHandler.cancel(10L, "el comprobante no corresponde");

		assertThat(response.status()).isEqualTo("CANCELLED");
	}

	@Test
	void GetReceipt_WhenNoReceipt() {
		when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment()));

		assertThatThrownBy(() -> appointmentServiceHandler.getReceipt(10L))
				.isInstanceOf(AppointmentReceiptNotFoundException.class);
	}

	@Test
	void GetReceipt_Successful() {
		when(appointmentRepository.findById(10L)).thenReturn(Optional.of(confirmedAppointment()));
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
		when(r2StorageClient.presignedGetUrl("appointments/1/comprobante.pdf"))
				.thenReturn(new PresignedUrl("https://r2.example/comprobante.pdf", expiresAt));

		AppointmentReceiptResponseDto response = appointmentServiceHandler.getReceipt(10L);

		assertThat(response.url()).isEqualTo("https://r2.example/comprobante.pdf");
		assertThat(response.expiresAt()).isEqualTo(expiresAt);
		assertThat(response.fileName()).isEqualTo("comprobante.pdf");
	}

	@Test
	void Reschedule_Successful() {
		mockFind(confirmedAppointment());
		LocalDateTime newDate = SCHEDULED_AT.plusDays(2);

		AppointmentResponseDto response = appointmentServiceHandler.reschedule(10L, newDate);

		assertThat(response.scheduledAt()).isEqualTo(newDate);
		assertThat(response.status()).isEqualTo("SCHEDULED");
		verify(appointmentRepository).save(any(Appointment.class));
	}

	@Test
	void Cancel_Successful() {
		mockFind(confirmedAppointment());

		AppointmentResponseDto response = appointmentServiceHandler.cancel(10L, "viaja esa semana");

		assertThat(response.status()).isEqualTo("CANCELLED");
		assertThat(response.cancellationReason()).isEqualTo("viaja esa semana");
	}

	@Test
	void RegisterAttendance_WhenAttended() {
		mockFind(confirmedAppointment());

		assertThat(appointmentServiceHandler.registerAttendance(10L, true).status()).isEqualTo("COMPLETED");
	}

	@Test
	void RegisterAttendance_WhenMissed() {
		mockFind(confirmedAppointment());

		assertThat(appointmentServiceHandler.registerAttendance(10L, false).status()).isEqualTo("MISSED");
	}

	@Test
	void ListAppointments_WhenDateIsGiven() {
		LocalDate date = LocalDate.now().plusDays(3);
		when(appointmentRepository.findByDay(date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
				.thenReturn(List.of(appointment()));

		assertThat(appointmentServiceHandler.listAppointments(date)).hasSize(1);
		verify(appointmentRepository).findByDay(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
	}

	@Test
	void ListAppointments_WhenAll() {
		when(appointmentRepository.findAllByOrderByScheduledAtDesc()).thenReturn(List.of(appointment()));

		assertThat(appointmentServiceHandler.listAppointments(null)).hasSize(1);
	}

	@Test
	void ListAppointmentsByApplicant_Successful() {
		when(appointmentRepository.findByApplicantIdOrderByScheduledAtDesc(1L))
				.thenReturn(List.of(appointment()));

		assertThat(appointmentServiceHandler.listAppointmentsByApplicant(1L)).hasSize(1);
	}

	@Test
	void CountByStatus_DelegatesToRepository() {
		LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(2026, 2, 1, 0, 0);
		when(appointmentRepository.countByStatusAndScheduledAtBetween(AppointmentStatus.COMPLETED, from, to))
				.thenReturn(6L);

		assertThat(appointmentServiceHandler.countByStatus(AppointmentStatus.COMPLETED, from, to)).isEqualTo(6L);
	}
}
