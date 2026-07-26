package com.banco.anteojos.backend.business.applicants.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

// Inmutable tras la creación: una corrección de graduación es una receta nueva.
@Entity
@Table(name = "prescriptions")
@Getter
public class Prescription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long applicantId;

	private BigDecimal rightSphere;

	private BigDecimal rightCylinder;

	private Integer rightAxis;

	private BigDecimal leftSphere;

	private BigDecimal leftCylinder;

	private Integer leftAxis;

	private LocalDateTime createdAt;

	protected Prescription() {
	}

	public Prescription(Long applicantId, BigDecimal rightSphere, BigDecimal rightCylinder,
			Integer rightAxis, BigDecimal leftSphere, BigDecimal leftCylinder, Integer leftAxis) {
		this.applicantId = applicantId;
		this.rightSphere = rightSphere;
		this.rightCylinder = rightCylinder;
		this.rightAxis = rightAxis;
		this.leftSphere = leftSphere;
		this.leftCylinder = leftCylinder;
		this.leftAxis = leftAxis;
		this.createdAt = LocalDateTime.now();
	}
}
