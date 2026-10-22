package com.banco.anteojos.backend.business.donors.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "donors")
@Getter
public class Donor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private DonorType donorType;

	/** Nombre completo si es persona, razón social si es entidad. */
	private String name;

	/** DNI o CUIT según el tipo. Opcional: muchas donaciones son anónimas o informales. */
	private String documentNumber;

	private String phone;

	private String email;

	// Domicilio del remitente: lo pide la etiqueta de envío de donaciones por Vía Cargo.
	private String address;

	private String postalCode;

	private String city;

	private String province;

	private LocalDateTime createdAt;

	protected Donor() {
	}

	public Donor(DonorType donorType, String name, String documentNumber, String phone, String email,
			String address, String postalCode, String city, String province) {
		this.donorType = donorType;
		this.name = name.trim();
		this.documentNumber = documentNumber == null ? null : documentNumber.trim();
		this.phone = phone == null ? null : phone.trim();
		this.email = email == null ? null : email.trim().toLowerCase();
		this.address = address == null ? null : address.trim();
		this.postalCode = postalCode == null ? null : postalCode.trim();
		this.city = city == null ? null : city.trim();
		this.province = province == null ? null : province.trim();
		this.createdAt = LocalDateTime.now();
	}
}
