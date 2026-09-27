package com.banco.anteojos.backend.useCase.appointments.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.banco.anteojos.backend.business.appointments.entities.Appointment;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus;
import com.banco.anteojos.backend.business.appointments.exception.InvalidAppointmentTransitionException;

@Tag("unit")
class AppointmentTest {

	private static final LocalDateTime SCHEDULED_AT = LocalDateTime.now().plusDays(3);

	private Appointment appointment() {
		return new Appointment(1L, null, SCHEDULED_AT, "  trae la receta original  ");
	}

	// La mayoría de las transiciones (reprogramar, cancelar, asistencia) exigen un turno ya
	// aprobado: este helper lo deja en ese estado sin repetir el submit+approve en cada test.
	private Appointment confirmedAppointment() {
		Appointment appointment = appointment();
		appointment.submitReceiptForReview("appointments/1/comprobante.pdf", "application/pdf", "comprobante.pdf");
		appointment.approve();
		return appointment;
	}

	private Appointment pendingReviewAppointment() {
		Appointment appointment = appointment();
		appointment.submitReceiptForReview("appointments/1/comprobante.pdf", "application/pdf", "comprobante.pdf");
		return appointment;
	}

	@Test
	void Create_Successful() {
		Appointment appointment = appointment();

		assertThat(appointment.getApplicantId()).isEqualTo(1L);
		assertThat(appointment.getAssignmentId()).isNull();
		assertThat(appointment.getScheduledAt()).isEqualTo(SCHEDULED_AT);
		// Nace sin confirmar: recién pasa a SCHEDULED con el comprobante del bono contribución.
		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING_PAYMENT);
		assertThat(appointment.getNotes()).isEqualTo("trae la receta original");
		assertThat(appointment.getCreatedAt()).isNotNull();
	}

	@Test
	void Create_WhenNotesAreBlank() {
		assertThat(new Appointment(1L, null, SCHEDULED_AT, "   ").getNotes()).isNull();
	}

	@Test
	void Create_AsPickupAppointment() {
		assertThat(new Appointment(1L, 9L, SCHEDULED_AT, null).getAssignmentId()).isEqualTo(9L);
	}

	@Test
	void SubmitReceiptForReview_Successful() {
		Appointment appointment = appointment();

		appointment.submitReceiptForReview("appointments/1/comprobante.pdf", "application/pdf", "comprobante.pdf");

		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING_REVIEW);
		assertThat(appointment.getReceiptKey()).isEqualTo("appointments/1/comprobante.pdf");
		assertThat(appointment.getReceiptContentType()).isEqualTo("application/pdf");
		assertThat(appointment.getReceiptOriginalName()).isEqualTo("comprobante.pdf");
	}

	@Test
	void SubmitReceiptForReview_WhenAlreadySubmitted() {
		Appointment appointment = pendingReviewAppointment();

		assertThatThrownBy(() -> appointment.submitReceiptForReview("otra-key", "application/pdf", "otro.pdf"))
				.isInstanceOf(InvalidAppointmentTransitionException.class)
				.hasMessageContaining("ya tiene un comprobante cargado");
	}

	@Test
	void Approve_Successful() {
		Appointment appointment = pendingReviewAppointment();

		appointment.approve();

		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
	}

	@Test
	void Approve_WhenNotPendingReview() {
		Appointment appointment = appointment();

		assertThatThrownBy(appointment::approve)
				.isInstanceOf(InvalidAppointmentTransitionException.class)
				.hasMessageContaining("pendiente de revisión");
	}

	@Test
	void Cancel_WhenPendingReview() {
		Appointment appointment = pendingReviewAppointment();

		appointment.cancel("el comprobante no corresponde");

		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
		assertThat(appointment.getCancellationReason()).isEqualTo("el comprobante no corresponde");
	}

	@Test
	void Reschedule_Successful() {
		Appointment appointment = confirmedAppointment();
		LocalDateTime newDate = SCHEDULED_AT.plusDays(2);

		appointment.reschedule(newDate);

		// Reprogramar mueve el mismo turno: sigue agendado, no nace otro.
		assertThat(appointment.getScheduledAt()).isEqualTo(newDate);
		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
	}

	@Test
	void Reschedule_WhenPendingPayment() {
		Appointment appointment = appointment();

		assertThatThrownBy(() -> appointment.reschedule(SCHEDULED_AT.plusDays(1)))
				.isInstanceOf(InvalidAppointmentTransitionException.class)
				.hasMessageContaining("no está confirmado");
	}

	@Test
	void Cancel_Successful() {
		Appointment appointment = confirmedAppointment();

		appointment.cancel("  viaja esa semana  ");

		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
		assertThat(appointment.getCancellationReason()).isEqualTo("viaja esa semana");
	}

	@Test
	void Cancel_WhenReasonIsBlank() {
		Appointment appointment = confirmedAppointment();

		appointment.cancel("   ");

		assertThat(appointment.getCancellationReason()).isNull();
	}

	@Test
	void RegisterAttendance_WhenAttended() {
		Appointment appointment = confirmedAppointment();

		appointment.registerAttendance(true);

		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
	}

	@Test
	void RegisterAttendance_WhenMissed() {
		Appointment appointment = confirmedAppointment();

		appointment.registerAttendance(false);

		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.MISSED);
	}

	@Test
	void Reschedule_WhenCancelled() {
		Appointment appointment = confirmedAppointment();
		appointment.cancel("no viene más");

		assertThatThrownBy(() -> appointment.reschedule(SCHEDULED_AT.plusDays(1)))
				.isInstanceOf(InvalidAppointmentTransitionException.class)
				.hasMessageContaining("cancelado");
	}

	@Test
	void Cancel_WhenAlreadyCompleted() {
		Appointment appointment = confirmedAppointment();
		appointment.registerAttendance(true);

		assertThatThrownBy(() -> appointment.cancel("tarde"))
				.isInstanceOf(InvalidAppointmentTransitionException.class)
				.hasMessageContaining("ya fue atendido");
	}

	@Test
	void RegisterAttendance_WhenAlreadyMissed() {
		Appointment appointment = confirmedAppointment();
		appointment.registerAttendance(false);

		assertThatThrownBy(() -> appointment.registerAttendance(true))
				.isInstanceOf(InvalidAppointmentTransitionException.class)
				.hasMessageContaining("ausente");
	}
}
