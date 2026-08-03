package com.banco.anteojos.backend.persistence.applicants;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.applicants.entities.AnsesCertificate;

public interface AnsesCertificatePostgresSqlRepository extends JpaRepository<AnsesCertificate, Long> {

	Optional<AnsesCertificate> findByApplicantId(Long applicantId);
}
