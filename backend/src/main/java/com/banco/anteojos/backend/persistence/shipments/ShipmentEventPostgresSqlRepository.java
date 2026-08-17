package com.banco.anteojos.backend.persistence.shipments;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.shipments.entities.ShipmentEvent;

public interface ShipmentEventPostgresSqlRepository extends JpaRepository<ShipmentEvent, Long> {

	/** En orden de llegada: occurred_at puede venir vacío del carrier, el id nunca. */
	List<ShipmentEvent> findByShipmentIdOrderByIdAsc(Long shipmentId);
}
