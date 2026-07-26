package com.banco.anteojos.backend.persistence.applicants;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.applicants.entities.Prescription;

public interface PrescriptionPostgresSqlRepository extends JpaRepository<Prescription, Long> {

	List<Prescription> findByApplicantId(Long applicantId);

	// Filtrar también por applicant valida la pertenencia: evita leer la receta de otro
	// solicitante armando una URL con IDs cruzados.
	Optional<Prescription> findByIdAndApplicantId(Long id, Long applicantId);
}
