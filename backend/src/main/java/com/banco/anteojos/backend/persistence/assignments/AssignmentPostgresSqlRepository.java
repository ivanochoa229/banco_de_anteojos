package com.banco.anteojos.backend.persistence.assignments;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.banco.anteojos.backend.business.assignments.entities.Assignment;

public interface AssignmentPostgresSqlRepository extends JpaRepository<Assignment, Long> {

	List<Assignment> findByApplicantIdOrderByAssignedAtDesc(Long applicantId);

	List<Assignment> findAllByOrderByAssignedAtDesc();

	/** En curso: ni entregadas ni canceladas. Es la cola de trabajo diaria del operador. */
	@Query("SELECT a FROM Assignment a WHERE a.deliveredAt IS NULL AND a.cancelledAt IS NULL "
			+ "ORDER BY a.assignedAt DESC")
	List<Assignment> findLive();
}
