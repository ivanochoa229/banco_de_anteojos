package com.banco.anteojos.backend.persistence.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.catalog.entities.Sale;

public interface SalePostgresSqlRepository extends JpaRepository<Sale, Long> {

	Page<Sale> findByProductIdOrderBySoldAtDescIdDesc(Long productId, Pageable pageable);
}
