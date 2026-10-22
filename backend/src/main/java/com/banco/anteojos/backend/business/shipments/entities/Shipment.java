package com.banco.anteojos.backend.business.shipments.entities;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.banco.anteojos.backend.business.shipments.exception.FrameFromAnotherDonorException;
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
 * Paquete con un conjunto de marcos que viaja por Vía Cargo.
 *
 * <p>Saliente (entre sucursales): nace PENDING mientras se arma; al despacharlo se le carga el
 * número de seguimiento y pasa a REGISTERED; desde ahí el estado lo mueve el webhook de 17TRACK,
 * nunca el operador.
 *
 * <p>Entrante (donación): lo despacha el donante, así que nace REGISTERED con la guía que él
 * informa y sin marcos (no se sabe qué trae hasta abrirlo). Lo cierra el operador al recibirlo,
 * con los marcos que cargó al inventario.
 */
@Entity
@Table(name = "shipments")
@Getter
public class Shipment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private ShipmentDirection direction;

	/** Solo en los entrantes: quién donó lo que viene adentro. FK plana, como en frames. */
	private Long donorId;

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

	private LocalDateTime receivedAt;

	// EAGER a propósito: el conjunto es chico y los servicios arman DTOs fuera de una
	// transacción, donde una colección LAZY explota.
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "shipment_frames", joinColumns = @JoinColumn(name = "shipment_id"))
	@Column(name = "frame_id")
	private Set<Long> frameIds = new LinkedHashSet<>();

	protected Shipment() {
	}

	public Shipment(String originBranch, String destinationBranch, List<Long> frameIds, String notes) {
		this.direction = ShipmentDirection.OUTBOUND;
		this.originBranch = originBranch.trim();
		this.destinationBranch = destinationBranch.trim();
		this.frameIds = new LinkedHashSet<>(frameIds);
		this.notes = notes == null || notes.isBlank() ? null : notes.trim();
		this.status = ShipmentStatus.PENDING;
		this.createdAt = LocalDateTime.now();
	}

	/**
	 * Donación en camino. dispatchedAt marca cuándo se cargó la guía, no cuándo el donante dejó
	 * el paquete en Vía Cargo: ese dato llega después con los eventos del carrier.
	 */
	public static Shipment inbound(Long donorId, String trackingNumber, String notes) {
		Shipment shipment = new Shipment();
		shipment.direction = ShipmentDirection.INBOUND;
		shipment.donorId = donorId;
		shipment.trackingNumber = trackingNumber.trim();
		shipment.notes = notes == null || notes.isBlank() ? null : notes.trim();
		shipment.status = ShipmentStatus.REGISTERED;
		shipment.createdAt = LocalDateTime.now();
		shipment.dispatchedAt = shipment.createdAt;
		return shipment;
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
		// La recepción la confirma el operador con el paquete abierto: un push tardío del
		// carrier (p. ej. el "entregado") no la pisa. El evento igual queda en el historial.
		if (status == ShipmentStatus.RECEIVED) {
			return;
		}
		this.status = newStatus;
		if (newStatus == ShipmentStatus.DELIVERED && deliveredAt == null) {
			this.deliveredAt = LocalDateTime.now();
		}
	}

	/**
	 * El operador abrió la donación y cargó los marcos al inventario. Puede pasar en cualquier
	 * estado del carrier: el paquete suele llegar antes de que 17TRACK informe la entrega, o
	 * 17TRACK no la informa nunca. Sin marcos también vale: el paquete pudo traer solo cosas
	 * que no sirven.
	 *
	 * @param frameDonorIds los donantes de esos marcos, para verificar que sean de esta donación
	 */
	public void receive(Collection<Long> frameIds, Collection<Long> frameDonorIds) {
		if (direction != ShipmentDirection.INBOUND) {
			throw new InvalidShipmentTransitionException("Solo se reciben las donaciones que llegan por envío");
		}
		if (status == ShipmentStatus.RECEIVED) {
			throw new InvalidShipmentTransitionException("La donación ya fue recibida");
		}
		if (!frameDonorIds.stream().allMatch(donorId::equals)) {
			throw new FrameFromAnotherDonorException();
		}
		this.frameIds = new LinkedHashSet<>(frameIds);
		this.status = ShipmentStatus.RECEIVED;
		this.receivedAt = LocalDateTime.now();
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
