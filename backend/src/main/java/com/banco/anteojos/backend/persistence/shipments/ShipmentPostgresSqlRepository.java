package com.banco.anteojos.backend.persistence.shipments;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.shipments.entities.Shipment;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentDirection;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentStatus;

public interface ShipmentPostgresSqlRepository extends JpaRepository<Shipment, Long> {

	Page<Shipment> findByDirectionOrderByCreatedAtDescIdDesc(ShipmentDirection direction, Pageable pageable);

	Page<Shipment> findByDirectionAndStatusNotInOrderByCreatedAtDescIdDesc(ShipmentDirection direction,
			Collection<ShipmentStatus> closedStatuses, Pageable pageable);

	Page<Shipment> findByDonorIdOrderByCreatedAtDescIdDesc(Long donorId, Pageable pageable);

	Optional<Shipment> findByTrackingNumber(String trackingNumber);

	boolean existsByTrackingNumber(String trackingNumber);

	long countByDirectionAndStatusAndDeliveredAtBetween(ShipmentDirection direction, ShipmentStatus status,
			LocalDateTime from, LocalDateTime to);
}
