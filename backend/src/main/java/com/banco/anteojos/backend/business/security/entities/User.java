package com.banco.anteojos.backend.business.security.entities;

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
@Table(name = "users")
@Getter
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String email;

	private String passwordHash;

	@Enumerated(EnumType.STRING)
	private Role role;

	private boolean isActive;

	private LocalDateTime createdAt;

	protected User() {
	}

	public User(String name, String email, String passwordHash, Role role) {
		this.name = name.trim();
		this.email = email.trim().toLowerCase();
		this.passwordHash = passwordHash;
		this.role = role;
		this.isActive = true;
		this.createdAt = LocalDateTime.now();
	}

	public void activate() {
		this.isActive = true;
	}

	public void deactivate() {
		this.isActive = false;
	}

	public void changePassword(String newHash) {
		this.passwordHash = newHash;
	}
}
