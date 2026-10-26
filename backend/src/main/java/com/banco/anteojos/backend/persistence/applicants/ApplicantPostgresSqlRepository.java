package com.banco.anteojos.backend.persistence.applicants;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banco.anteojos.backend.business.applicants.entities.Applicant;
import com.banco.anteojos.backend.business.common.SearchTerms;

public interface ApplicantPostgresSqlRepository extends JpaRepository<Applicant, Long> {

	Optional<Applicant> findByDni(String dni);

	/**
	 * Por apellido y nombre en cualquier orden, sin tildes ni mayúsculas, o por el comienzo del DNI.
	 * {@code search} llega normalizado por {@link SearchTerms#normalize}; vacío trae a todos.
	 */
	@Query("SELECT a FROM Applicant a WHERE "
			+ "translate(lower(concat(a.lastName, ' ', a.firstName)), '" + SearchTerms.ACCENTED + "', '"
			+ SearchTerms.PLAIN + "') LIKE concat('%', :search, '%') "
			+ "OR translate(lower(concat(a.firstName, ' ', a.lastName)), '" + SearchTerms.ACCENTED + "', '"
			+ SearchTerms.PLAIN + "') LIKE concat('%', :search, '%') "
			+ "OR a.dni LIKE concat(:search, '%') "
			+ "ORDER BY a.lastName, a.firstName, a.id")
	Page<Applicant> search(@Param("search") String search, Pageable pageable);
}
