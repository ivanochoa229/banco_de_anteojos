package com.banco.anteojos.backend.persistence.shipments;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.shipments.entities.Shipment;

public interface ShipmentPostgresSqlRepository extends JpaRepository<Shipment, Long> {

	List<Shipment> findAllByOrderByCreatedAtDesc();

	Optional<Shipment> findByTrackingNumber(String trackingNumber);

	boolean existsByTrackingNumber(String trackingNumber);
}
