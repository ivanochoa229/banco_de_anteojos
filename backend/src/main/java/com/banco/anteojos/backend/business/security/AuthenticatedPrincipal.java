package com.banco.anteojos.backend.business.security;

// Principal que arma el JwtAuthenticationFilter a partir del token: todo lo que el resto del
// código puede saber de "quién hace este request" sin volver a tocar la base.
public record AuthenticatedPrincipal(String email, String role, Long applicantId) {
}
