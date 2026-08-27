package com.banco.anteojos.backend.persistence.shipments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.shipments.entities.Shipment;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentStatus;

public interface ShipmentPostgresSqlRepository extends JpaRepository<Shipment, Long> {

	List<Shipment> findAllByOrderByCreatedAtDesc();

	Optional<Shipment> findByTrackingNumber(String trackingNumber);

	boolean existsByTrackingNumber(String trackingNumber);

	long countByStatusAndDeliveredAtBetween(ShipmentStatus status, LocalDateTime from, LocalDateTime to);
}
