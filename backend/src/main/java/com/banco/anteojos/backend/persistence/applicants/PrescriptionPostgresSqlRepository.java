package com.banco.anteojos.backend.persistence.applicants;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.applicants.entities.Prescription;

public interface PrescriptionPostgresSqlRepository extends JpaRepository<Prescription, Long> {

	List<Prescription> findByApplicantId(Long applicantId);
}
