package com.banco.anteojos.backend.persistence.applicants;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.applicants.entities.Applicant;

public interface ApplicantPostgresSqlRepository extends JpaRepository<Applicant, Long> {

	Optional<Applicant> findByDni(String dni);
}
