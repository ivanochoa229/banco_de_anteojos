package com.banco.anteojos.backend.business.applicants.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

// La graduación es inmutable tras la creación: una corrección es una receta nueva.
// El archivo adjunto sí se puede reemplazar (un escaneo ilegible se vuelve a sacar) y no
// altera la graduación, así que no justifica una receta nueva.
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

	// Key del objeto en R2, no una URL: el bucket es privado y las presigned URLs vencen.
	private String fileKey;

	private String fileContentType;

	private String fileOriginalName;

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

	/** Devuelve la key anterior (null si no había) para que el service borre el objeto huérfano. */
	public String attachFile(String fileKey, String fileContentType, String fileOriginalName) {
		String previousKey = this.fileKey;
		this.fileKey = fileKey;
		this.fileContentType = fileContentType;
		this.fileOriginalName = fileOriginalName;
		return previousKey;
	}

	public boolean hasFile() {
		return fileKey != null;
	}
}
