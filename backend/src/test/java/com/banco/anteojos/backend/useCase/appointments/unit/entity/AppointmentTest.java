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

	@Test
	void Create_Successful() {
		Appointment appointment = appointment();

		assertThat(appointment.getApplicantId()).isEqualTo(1L);
		assertThat(appointment.getAssignmentId()).isNull();
		assertThat(appointment.getScheduledAt()).isEqualTo(SCHEDULED_AT);
		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
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
	void Reschedule_Successful() {
		Appointment appointment = appointment();
		LocalDateTime newDate = SCHEDULED_AT.plusDays(2);

		appointment.reschedule(newDate);

		// Reprogramar mueve el mismo turno: sigue agendado, no nace otro.
		assertThat(appointment.getScheduledAt()).isEqualTo(newDate);
		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
	}

	@Test
	void Cancel_Successful() {
		Appointment appointment = appointment();

		appointment.cancel("  viaja esa semana  ");

		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
		assertThat(appointment.getCancellationReason()).isEqualTo("viaja esa semana");
	}

	@Test
	void Cancel_WhenReasonIsBlank() {
		Appointment appointment = appointment();

		appointment.cancel("   ");

		assertThat(appointment.getCancellationReason()).isNull();
	}

	@Test
	void RegisterAttendance_WhenAttended() {
		Appointment appointment = appointment();

		appointment.registerAttendance(true);

		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
	}

	@Test
	void RegisterAttendance_WhenMissed() {
		Appointment appointment = appointment();

		appointment.registerAttendance(false);

		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.MISSED);
	}

	@Test
	void Reschedule_WhenCancelled() {
		Appointment appointment = appointment();
		appointment.cancel("no viene más");

		assertThatThrownBy(() -> appointment.reschedule(SCHEDULED_AT.plusDays(1)))
				.isInstanceOf(InvalidAppointmentTransitionException.class)
				.hasMessageContaining("cancelado");
	}

	@Test
	void Cancel_WhenAlreadyCompleted() {
		Appointment appointment = appointment();
		appointment.registerAttendance(true);

		assertThatThrownBy(() -> appointment.cancel("tarde"))
				.isInstanceOf(InvalidAppointmentTransitionException.class)
				.hasMessageContaining("ya fue atendido");
	}

	@Test
	void RegisterAttendance_WhenAlreadyMissed() {
		Appointment appointment = appointment();
		appointment.registerAttendance(false);

		assertThatThrownBy(() -> appointment.registerAttendance(true))
				.isInstanceOf(InvalidAppointmentTransitionException.class)
				.hasMessageContaining("ausente");
	}
}
