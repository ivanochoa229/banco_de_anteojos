package com.banco.anteojos.backend.persistence.donors;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.donors.entities.Donor;

public interface DonorPostgresSqlRepository extends JpaRepository<Donor, Long> {

	List<Donor> findAllByOrderByNameAsc();
}
