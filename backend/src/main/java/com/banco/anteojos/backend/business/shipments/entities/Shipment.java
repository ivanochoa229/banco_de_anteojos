package com.banco.anteojos.backend.business.shipments.entities;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.banco.anteojos.backend.business.shipments.exception.InvalidShipmentTransitionException;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * Paquete con un conjunto de marcos que viaja entre sucursales por Vía Cargo. Nace PENDING
 * mientras se arma; al despacharlo se le carga el número de seguimiento y pasa a REGISTERED;
 * desde ahí el estado lo mueve el webhook de 17TRACK, nunca el operador.
 */
@Entity
@Table(name = "shipments")
@Getter
public class Shipment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String originBranch;

	private String destinationBranch;

	private String trackingNumber;

	@Enumerated(EnumType.STRING)
	private ShipmentStatus status;

	private String notes;

	private String cancellationReason;

	private LocalDateTime createdAt;

	private LocalDateTime dispatchedAt;

	private LocalDateTime deliveredAt;

	// EAGER a propósito: el conjunto es chico y los servicios arman DTOs fuera de una
	// transacción, donde una colección LAZY explota.
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "shipment_frames", joinColumns = @JoinColumn(name = "shipment_id"))
	@Column(name = "frame_id")
	private Set<Long> frameIds = new LinkedHashSet<>();

	protected Shipment() {
	}

	public Shipment(String originBranch, String destinationBranch, List<Long> frameIds, String notes) {
		this.originBranch = originBranch.trim();
		this.destinationBranch = destinationBranch.trim();
		this.frameIds = new LinkedHashSet<>(frameIds);
		this.notes = notes == null || notes.isBlank() ? null : notes.trim();
		this.status = ShipmentStatus.PENDING;
		this.createdAt = LocalDateTime.now();
	}

	/** El despacho físico: Vía Cargo ya emitió el número de seguimiento. */
	public void dispatch(String trackingNumber) {
		requirePending();
		this.trackingNumber = trackingNumber.trim();
		this.status = ShipmentStatus.REGISTERED;
		this.dispatchedAt = LocalDateTime.now();
	}

	public void cancel(String reason) {
		requirePending();
		this.status = ShipmentStatus.CANCELLED;
		this.cancellationReason = reason == null || reason.isBlank() ? null : reason.trim();
	}

	/** Solo lo llama el webhook: después del despacho, el estado es del carrier. */
	public void applyTrackingUpdate(ShipmentStatus newStatus) {
		if (status == ShipmentStatus.PENDING || status == ShipmentStatus.CANCELLED) {
			throw new InvalidShipmentTransitionException("El envío no está despachado");
		}
		this.status = newStatus;
		if (newStatus == ShipmentStatus.DELIVERED && deliveredAt == null) {
			this.deliveredAt = LocalDateTime.now();
		}
	}

	private void requirePending() {
		if (status == ShipmentStatus.CANCELLED) {
			throw new InvalidShipmentTransitionException("El envío está cancelado");
		}
		if (status != ShipmentStatus.PENDING) {
			throw new InvalidShipmentTransitionException("El envío ya fue despachado");
		}
	}
}
