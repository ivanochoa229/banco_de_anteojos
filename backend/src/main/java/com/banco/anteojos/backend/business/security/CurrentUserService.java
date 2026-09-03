package com.banco.anteojos.backend.business.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// Único punto por el que un endpoint de autogestión (/v1/me/**) sabe de qué solicitante es el
// token: evita recibir el applicantId por path, que dependería de que el cliente no mienta.
@Component
public class CurrentUserService {

	public Long getApplicantId() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal) {
			return principal.applicantId();
		}
		return null;
	}
}
