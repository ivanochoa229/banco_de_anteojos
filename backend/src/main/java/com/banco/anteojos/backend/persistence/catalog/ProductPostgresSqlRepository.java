package com.banco.anteojos.backend.persistence.catalog;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banco.anteojos.backend.business.catalog.entities.Product;
import com.banco.anteojos.backend.business.catalog.entities.ProductStatus;

import jakarta.persistence.LockModeType;

public interface ProductPostgresSqlRepository extends JpaRepository<Product, Long> {

	Page<Product> findAllByOrderByNameAscIdAsc(Pageable pageable);

	Page<Product> findByStatusOrderByNameAscIdAsc(ProductStatus status, Pageable pageable);

	/**
	 * Serializa las ventas sobre un mismo producto: dos operadores vendiendo a la vez el mismo
	 * anteojo lo leerían los dos a la venta. Con el producto bloqueado, el segundo espera y lo ve
	 * ya vendido.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Product p WHERE p.id = :id")
	Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
