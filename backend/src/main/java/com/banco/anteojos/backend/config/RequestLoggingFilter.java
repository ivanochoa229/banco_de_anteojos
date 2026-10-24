package com.banco.anteojos.backend.config;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Deja el contexto de la petición en el MDC (el patrón de logging lo imprime en cada línea, venga
 * de donde venga) y loguea una línea por petición al terminar, con status y duración. No se
 * loguean bodies ni query string: pueden traer datos personales o tokens.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

	public static final String TRACE_ID_HEADER = "X-Trace-Id";

	// Solo caracteres seguros: evita inyección de líneas de log por header y valores larguísimos.
	private static final Pattern SAFE_TRACE_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		// Los healthchecks de Docker/Caddy pegan cada pocos segundos: ensuciarían el log.
		return request.getRequestURI().startsWith("/actuator/health");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String traceId = resolveTraceId(request.getHeader(TRACE_ID_HEADER));
		long startedAt = System.nanoTime();
		try {
			MDC.put("traceId", traceId);
			MDC.put("username", "anonymous"); // JwtAuthenticationFilter lo pisa si hay token válido
			MDC.put("sourceIp", request.getRemoteAddr());
			MDC.put("httpMethod", request.getMethod());
			MDC.put("path", request.getRequestURI());
			response.setHeader(TRACE_ID_HEADER, traceId); // para correlacionar desde el front
			try {
				filterChain.doFilter(request, response);
			} finally {
				long millis = (System.nanoTime() - startedAt) / 1_000_000;
				log.info("Petición {} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(),
						response.getStatus(), millis);
			}
		} finally {
			MDC.clear(); // los threads del servlet se reutilizan
		}
	}

	private String resolveTraceId(String headerValue) {
		if (headerValue != null && SAFE_TRACE_ID.matcher(headerValue.trim()).matches()) {
			return headerValue.trim();
		}
		return UUID.randomUUID().toString();
	}
}
