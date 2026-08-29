package com.banco.anteojos.backend.persistence.catalog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.catalog.entities.Product;
import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;

public interface ProductPostgresSqlRepository extends JpaRepository<Product, Long> {

	List<Product> findAllByOrderByNameAsc();

	List<Product> findByStatusOrderByNameAsc(ProductStatus status);
}
