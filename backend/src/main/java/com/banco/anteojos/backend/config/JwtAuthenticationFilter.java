package com.banco.anteojos.backend.config;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.banco.anteojos.backend.business.security.AuthenticatedPrincipal;
import com.banco.anteojos.backend.business.security.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith(BEARER_PREFIX)) {
			jwtService.validate(header.substring(BEARER_PREFIX.length()))
					.ifPresent(tokenData -> SecurityContextHolder.getContext().setAuthentication(
							new UsernamePasswordAuthenticationToken(
									new AuthenticatedPrincipal(tokenData.email(), tokenData.role(), tokenData.applicantId()),
									null,
									List.of(new SimpleGrantedAuthority("ROLE_" + tokenData.role())))));
		}
		filterChain.doFilter(request, response);
	}
}
