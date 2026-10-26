package com.banco.anteojos.backend.persistence.assignments;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banco.anteojos.backend.business.assignments.entities.FrameReservation;

public interface FrameReservationPostgresSqlRepository extends JpaRepository<FrameReservation, Long> {

	String ACTIVE = "r.cancelledAt IS NULL AND r.expiredAt IS NULL AND r.fulfilledAt IS NULL";

	@Query("SELECT r FROM FrameReservation r WHERE r.applicantId = :applicantId AND " + ACTIVE)
	Optional<FrameReservation> findActiveByApplicantId(@Param("applicantId") Long applicantId);

	/** Las que siguen activas con el plazo cumplido: la tarea programada las libera. */
	@Query("SELECT r FROM FrameReservation r WHERE r.expiresAt < :now AND " + ACTIVE)
	List<FrameReservation> findOverdue(@Param("now") LocalDateTime now);
}
