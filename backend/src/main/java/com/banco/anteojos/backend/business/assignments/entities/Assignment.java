package com.banco.anteojos.backend.business.assignments.entities;

import java.time.LocalDateTime;

import com.banco.anteojos.backend.business.assignments.exception.InvalidAssignmentTransitionException;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * Recorrido de un par de anteojos: se asigna un marco a un beneficiario, se manda con la receta
 * a la óptica que coloca los cristales, vuelve terminado y se entrega. Cada hito es un timestamp
 * en vez de un estado: así la trazabilidad de RF-16 sale de la propia fila y no hay que
 * mantenerla sincronizada con el estado del marco.
 */
@Entity
@Table(name = "assignments")
@Getter
public class Assignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long applicantId;

	private Long frameId;

	private Long prescriptionId;

	private LocalDateTime assignedAt;

	private LocalDateTime sentToOpticianAt;

	private LocalDateTime returnedAt;

	private LocalDateTime deliveredAt;

	private LocalDateTime cancelledAt;

	private String cancellationReason;

	private String notes;

	protected Assignment() {
	}

	public Assignment(Long applicantId, Long frameId, Long prescriptionId, String notes) {
		this.applicantId = applicantId;
		this.frameId = frameId;
		this.prescriptionId = prescriptionId;
		this.notes = notes == null || notes.isBlank() ? null : notes.trim();
		this.assignedAt = LocalDateTime.now();
	}

	public void sendToOptician() {
		requireOpen();
		if (sentToOpticianAt != null) {
			throw new InvalidAssignmentTransitionException("El marco ya fue enviado a la óptica");
		}
		this.sentToOpticianAt = LocalDateTime.now();
	}

	public void returnFromOptician() {
		requireOpen();
		if (sentToOpticianAt == null) {
			throw new InvalidAssignmentTransitionException("El marco todavía no fue enviado a la óptica");
		}
		if (returnedAt != null) {
			throw new InvalidAssignmentTransitionException("El marco ya volvió de la óptica");
		}
		this.returnedAt = LocalDateTime.now();
	}

	public void deliver() {
		requireOpen();
		// Se entrega el lente terminado, así que tiene que haber vuelto de la óptica con
		// los cristales puestos: el marco solo no le sirve al beneficiario.
		if (returnedAt == null) {
			throw new InvalidAssignmentTransitionException(
					"No se puede entregar: el marco todavía no volvió de la óptica con los cristales");
		}
		this.deliveredAt = LocalDateTime.now();
	}

	public void cancel(String reason) {
		requireOpen();
		this.cancelledAt = LocalDateTime.now();
		this.cancellationReason = reason == null || reason.isBlank() ? null : reason.trim();
	}

	/** Sigue en curso: ni entregada ni cancelada. */
	public boolean isLive() {
		return deliveredAt == null && cancelledAt == null;
	}

	private void requireOpen() {
		if (cancelledAt != null) {
			throw new InvalidAssignmentTransitionException("La asignación está cancelada");
		}
		if (deliveredAt != null) {
			throw new InvalidAssignmentTransitionException("La asignación ya fue entregada");
		}
	}
}
