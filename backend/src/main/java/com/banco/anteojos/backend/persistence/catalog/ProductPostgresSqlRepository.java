package com.banco.anteojos.backend.persistence.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.catalog.entities.Product;
import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;

public interface ProductPostgresSqlRepository extends JpaRepository<Product, Long> {

	Page<Product> findAllByOrderByNameAscIdAsc(Pageable pageable);

	Page<Product> findByStatusOrderByNameAscIdAsc(ProductStatus status, Pageable pageable);
}
