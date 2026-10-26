package com.banco.anteojos.backend.persistence.frames;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banco.anteojos.backend.business.frames.entities.Frame;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;

public interface FramePostgresSqlRepository extends JpaRepository<Frame, Long> {

	Optional<Frame> findBySealCode(String sealCode);

	/**
	 * Filtros opcionales: {@code status} null no filtra; {@code search} (parte del precinto, en
	 * minúsculas) vacío tampoco. {@code withImage} deja solo los que tienen foto para el probador.
	 */
	@Query("SELECT f FROM Frame f WHERE (:status IS NULL OR f.status = :status) "
			+ "AND (:withImage = false OR f.imageKey IS NOT NULL) "
			+ "AND lower(f.sealCode) LIKE concat('%', :search, '%') "
			+ "ORDER BY f.receivedAt DESC, f.id DESC")
	Page<Frame> search(@Param("status") FrameStatus status, @Param("withImage") boolean withImage,
			@Param("search") String search, Pageable pageable);

	@Query("SELECT f FROM Frame f WHERE f.donorId = :donorId AND (:status IS NULL OR f.status = :status) "
			+ "ORDER BY f.receivedAt DESC, f.id DESC")
	Page<Frame> findByDonor(@Param("donorId") Long donorId, @Param("status") FrameStatus status,
			Pageable pageable);

	long countByReceivedAtBetween(LocalDateTime from, LocalDateTime to);
}
