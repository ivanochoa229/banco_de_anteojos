package com.banco.anteojos.backend.persistence.catalog;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.catalog.entities.Sale;

public interface SalePostgresSqlRepository extends JpaRepository<Sale, Long> {

	Optional<Sale> findByProductId(Long productId);
}
