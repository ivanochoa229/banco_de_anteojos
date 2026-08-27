package com.banco.anteojos.backend.persistence.assignments;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banco.anteojos.backend.business.assignments.entities.Assignment;

public interface AssignmentPostgresSqlRepository extends JpaRepository<Assignment, Long> {

	List<Assignment> findByApplicantIdOrderByAssignedAtDesc(Long applicantId);

	List<Assignment> findAllByOrderByAssignedAtDesc();

	/** En curso: ni entregadas ni canceladas. Es la cola de trabajo diaria del operador. */
	@Query("SELECT a FROM Assignment a WHERE a.deliveredAt IS NULL AND a.cancelledAt IS NULL "
			+ "ORDER BY a.assignedAt DESC")
	List<Assignment> findLive();

	@Query("SELECT COUNT(a) FROM Assignment a WHERE a.deliveredAt BETWEEN :from AND :to")
	long countDelivered(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT COUNT(DISTINCT a.applicantId) FROM Assignment a WHERE a.deliveredAt BETWEEN :from AND :to")
	long countDistinctApplicantsServed(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Entregas y beneficiarios distintos por mes (RF-27). Nativa porque {@code date_trunc} no es
	 * JPQL portable; los tests corren contra Postgres real, no hay problema de portabilidad.
	 */
	@Query(nativeQuery = true, value = """
			SELECT date_trunc('month', delivered_at) AS month,
			       COUNT(*) AS deliveries,
			       COUNT(DISTINCT applicant_id) AS applicants
			FROM assignments
			WHERE delivered_at BETWEEN :from AND :to
			GROUP BY 1
			ORDER BY 1
			""")
	List<MonthlyDeliveryProjection> deliveriesByMonth(@Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to);
}
