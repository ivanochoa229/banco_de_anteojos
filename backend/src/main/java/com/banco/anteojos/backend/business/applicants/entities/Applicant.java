package com.banco.anteojos.backend.business.applicants.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "applicants")
@Getter
public class Applicant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String firstName;

	private String lastName;

	// El DNI es la identidad contra la que se valida RENAPER: se fija al crear y no se edita.
	private String dni;

	private String cuil;

	private LocalDate birthDate;

	private String phone;

	private String email;

	private boolean identityValidated;

	private LocalDateTime createdAt;

	protected Applicant() {
	}

	public Applicant(String firstName, String lastName, String dni, LocalDate birthDate,
			String phone, String email) {
		this.firstName = firstName.trim();
		this.lastName = lastName.trim();
		this.dni = dni.trim();
		this.birthDate = birthDate;
		this.phone = phone == null ? null : phone.trim();
		this.email = email == null ? null : email.trim().toLowerCase();
		this.identityValidated = false;
		this.createdAt = LocalDateTime.now();
	}

	public void updatePersonalData(String firstName, String lastName, LocalDate birthDate,
			String phone, String email) {
		this.firstName = firstName.trim();
		this.lastName = lastName.trim();
		this.birthDate = birthDate;
		this.phone = phone == null ? null : phone.trim();
		this.email = email == null ? null : email.trim().toLowerCase();
	}
}
