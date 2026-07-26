package com.banco.anteojos.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.banco.anteojos.backend.business.security.SecurityService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AdminBootstrap implements CommandLineRunner {

	private final SecurityService securityService;
	private final String adminEmail;
	private final String adminPassword;

	public AdminBootstrap(SecurityService securityService,
			@Value("${bootstrap.admin.email}") String adminEmail,
			@Value("${bootstrap.admin.password}") String adminPassword) {
		this.securityService = securityService;
		this.adminEmail = adminEmail;
		this.adminPassword = adminPassword;
	}

	@Override
	public void run(String... args) {
		if (adminEmail.isBlank() || adminPassword.isBlank()) {
			log.warn("ADMIN_EMAIL / ADMIN_PASSWORD no configurados: se omite el bootstrap del admin inicial");
			return;
		}
		securityService.createInitialAdmin(adminEmail, adminPassword);
	}
}
