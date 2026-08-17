package com.banco.anteojos.backend.business.shipments.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * Un evento del carrier tal como lo empujó 17TRACK (RF-25). El status se guarda crudo: la
 * trazabilidad tiene que reflejar lo que informó el carrier, no nuestro mapeo.
 */
@Entity
@Table(name = "shipment_events")
@Getter
public class ShipmentEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long shipmentId;

	private String status;

	private String description;

	private String location;

	/** Cuándo ocurrió según el carrier; null si el push no lo trajo. */
	private LocalDateTime occurredAt;

	private LocalDateTime receivedAt;

	protected ShipmentEvent() {
	}

	public ShipmentEvent(Long shipmentId, String status, String description, String location,
			LocalDateTime occurredAt) {
		this.shipmentId = shipmentId;
		this.status = status;
		this.description = description;
		this.location = location;
		this.occurredAt = occurredAt;
		this.receivedAt = LocalDateTime.now();
	}
}
