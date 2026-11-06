package com.banco.anteojos.backend.business.assignments.entities;

import java.time.LocalDateTime;

import com.banco.anteojos.backend.business.assignments.exception.InvalidAssignmentTransitionException;

import jakarta.persistence.Column;
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

	// Responsable de cada hito (RF-16 / RNF-09): el usuario que lo registró. Nulos solo en las
	// filas anteriores a la auditoría.
	private Long assignedByUserId;

	private Long sentToOpticianByUserId;

	private Long returnedByUserId;

	private Long deliveredByUserId;

	private Long cancelledByUserId;

	private LocalDateTime sentToOpticianAt;

	private LocalDateTime returnedAt;

	private LocalDateTime deliveredAt;

	private LocalDateTime cancelledAt;

	private String cancellationReason;

	private String notes;

	private String deliveryPhotoKey;

	private String deliveryPhotoContentType;

	private String deliveryPhotoOriginalName;

	@Column(name = "has_delivery_photo_consent")
	private boolean deliveryPhotoConsent;

	protected Assignment() {
	}

	public Assignment(Long applicantId, Long frameId, Long prescriptionId, String notes, Long userId) {
		this.applicantId = applicantId;
		this.frameId = frameId;
		this.prescriptionId = prescriptionId;
		this.notes = notes == null || notes.isBlank() ? null : notes.trim();
		this.assignedAt = LocalDateTime.now();
		this.assignedByUserId = userId;
	}

	public void sendToOptician(Long userId) {
		requireOpen();
		if (sentToOpticianAt != null) {
			throw new InvalidAssignmentTransitionException("El marco ya fue enviado a la óptica");
		}
		this.sentToOpticianAt = LocalDateTime.now();
		this.sentToOpticianByUserId = userId;
	}

	public void returnFromOptician(Long userId) {
		requireOpen();
		if (sentToOpticianAt == null) {
			throw new InvalidAssignmentTransitionException("El marco todavía no fue enviado a la óptica");
		}
		if (returnedAt != null) {
			throw new InvalidAssignmentTransitionException("El marco ya volvió de la óptica");
		}
		this.returnedAt = LocalDateTime.now();
		this.returnedByUserId = userId;
	}

	public void deliver(Long userId) {
		requireOpen();
		// Se entrega el lente terminado, así que tiene que haber vuelto de la óptica con
		// los cristales puestos: el marco solo no le sirve al beneficiario.
		if (returnedAt == null) {
			throw new InvalidAssignmentTransitionException(
					"No se puede entregar: el marco todavía no volvió de la óptica con los cristales");
		}
		this.deliveredAt = LocalDateTime.now();
		this.deliveredByUserId = userId;
	}

	/**
	 * Foto del beneficiario con sus anteojos, sacada al entregarlos. Solo tiene sentido sobre una
	 * entrega ya registrada.
	 */
	public void attachDeliveryPhoto(String key, String contentType, String originalName, boolean consent) {
		if (deliveredAt == null) {
			throw new InvalidAssignmentTransitionException(
					"La foto de la entrega se carga al registrar la entrega");
		}
		this.deliveryPhotoKey = key;
		this.deliveryPhotoContentType = contentType;
		this.deliveryPhotoOriginalName = originalName;
		this.deliveryPhotoConsent = consent;
	}

	public boolean hasDeliveryPhoto() {
		return deliveryPhotoKey != null;
	}

	public void cancel(String reason, Long userId) {
		requireOpen();
		this.cancelledAt = LocalDateTime.now();
		this.cancelledByUserId = userId;
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
