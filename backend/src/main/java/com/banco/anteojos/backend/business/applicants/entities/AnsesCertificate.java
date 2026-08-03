package com.banco.anteojos.backend.business.applicants.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

// Hay un solo certificado vigente por solicitante (UNIQUE en DB): la negativa vence a los
// 30 días, así que subir una nueva reemplaza a la anterior en lugar de acumular historia.
@Entity
@Table(name = "anses_certificates")
@Getter
public class AnsesCertificate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long applicantId;

	private String cuil;

	private String transactionNumber;

	private LocalDate issueDate;

	// Key del objeto en R2, no una URL: el bucket es privado y las presigned URLs vencen.
	private String fileKey;

	private String fileContentType;

	private String fileOriginalName;

	private LocalDateTime createdAt;

	protected AnsesCertificate() {
	}

	public AnsesCertificate(Long applicantId, String cuil, String transactionNumber, LocalDate issueDate,
			String fileKey, String fileContentType, String fileOriginalName) {
		this.applicantId = applicantId;
		this.cuil = cuil;
		this.transactionNumber = transactionNumber;
		this.issueDate = issueDate;
		this.fileKey = fileKey;
		this.fileContentType = fileContentType;
		this.fileOriginalName = fileOriginalName;
		this.createdAt = LocalDateTime.now();
	}

	/** Devuelve la key anterior para que el service borre el objeto huérfano en R2. */
	public String replaceWith(String cuil, String transactionNumber, LocalDate issueDate,
			String fileKey, String fileContentType, String fileOriginalName) {
		String previousKey = this.fileKey;
		this.cuil = cuil;
		this.transactionNumber = transactionNumber;
		this.issueDate = issueDate;
		this.fileKey = fileKey;
		this.fileContentType = fileContentType;
		this.fileOriginalName = fileOriginalName;
		this.createdAt = LocalDateTime.now();
		return previousKey;
	}
}
