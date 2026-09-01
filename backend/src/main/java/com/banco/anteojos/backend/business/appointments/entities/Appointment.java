package com.banco.anteojos.backend.business.appointments.entities;

import java.time.LocalDateTime;

import com.banco.anteojos.backend.business.appointments.exception.InvalidAppointmentTransitionException;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * Turno de atención de un beneficiario (RF-20/21). Reprogramar pisa la fecha en vez de crear otro
 * turno: para la fundación es el mismo compromiso movido de día, no dos. Si además apunta a una
 * asignación es un turno de retiro del par de anteojos terminado.
 */
@Entity
@Table(name = "appointments")
@Getter
public class Appointment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long applicantId;

	private Long assignmentId;

	private LocalDateTime scheduledAt;

	@Enumerated(EnumType.STRING)
	private AppointmentStatus status;

	private String notes;

	private String cancellationReason;

	private LocalDateTime createdAt;

	// Comprobante del bono contribución que confirma el turno. Nulos hasta que se sube.
	private String receiptKey;

	private String receiptContentType;

	private String receiptOriginalName;

	protected Appointment() {
	}

	public Appointment(Long applicantId, Long assignmentId, LocalDateTime scheduledAt, String notes) {
		this.applicantId = applicantId;
		this.assignmentId = assignmentId;
		this.scheduledAt = scheduledAt;
		this.notes = notes == null || notes.isBlank() ? null : notes.trim();
		this.status = AppointmentStatus.PENDING_PAYMENT;
		this.createdAt = LocalDateTime.now();
	}

	/** Solo se puede confirmar un turno que todavía no lo está: no admite reemplazar el comprobante. */
	public void confirmWithReceipt(String key, String contentType, String originalName) {
		if (status != AppointmentStatus.PENDING_PAYMENT) {
			throw new InvalidAppointmentTransitionException("El turno ya está confirmado");
		}
		this.receiptKey = key;
		this.receiptContentType = contentType;
		this.receiptOriginalName = originalName;
		this.status = AppointmentStatus.SCHEDULED;
	}

	public void reschedule(LocalDateTime newScheduledAt) {
		requireScheduled();
		this.scheduledAt = newScheduledAt;
	}

	public void cancel(String reason) {
		requireScheduled();
		this.status = AppointmentStatus.CANCELLED;
		this.cancellationReason = reason == null || reason.isBlank() ? null : reason.trim();
	}

	public void registerAttendance(boolean attended) {
		requireScheduled();
		this.status = attended ? AppointmentStatus.COMPLETED : AppointmentStatus.MISSED;
	}

	private void requireScheduled() {
		switch (status) {
			case PENDING_PAYMENT -> throw new InvalidAppointmentTransitionException(
					"El turno todavía no está confirmado: falta el comprobante");
			case CANCELLED -> throw new InvalidAppointmentTransitionException("El turno está cancelado");
			case COMPLETED -> throw new InvalidAppointmentTransitionException("El turno ya fue atendido");
			case MISSED -> throw new InvalidAppointmentTransitionException(
					"El turno ya quedó registrado como ausente");
			case SCHEDULED -> {
			}
		}
	}
}
