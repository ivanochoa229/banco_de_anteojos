package com.banco.anteojos.backend.persistence.donors;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banco.anteojos.backend.business.common.SearchTerms;
import com.banco.anteojos.backend.business.donors.entities.Donor;

public interface DonorPostgresSqlRepository extends JpaRepository<Donor, Long> {

	/** Por nombre, sin tildes ni mayúsculas. {@code search} llega normalizado; vacío trae a todos. */
	@Query("SELECT d FROM Donor d WHERE translate(lower(d.name), '" + SearchTerms.ACCENTED + "', '"
			+ SearchTerms.PLAIN + "') LIKE concat('%', :search, '%') ORDER BY d.name, d.id")
	Page<Donor> search(@Param("search") String search, Pageable pageable);

	Page<Donor> findByIdInOrderByNameAscIdAsc(Collection<Long> ids, Pageable pageable);
}
