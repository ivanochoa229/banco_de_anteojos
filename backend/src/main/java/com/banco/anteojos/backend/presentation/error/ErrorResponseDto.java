package com.banco.anteojos.backend.presentation.error;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

public record ErrorResponseDto(
		LocalDateTime timestamp,
		int status,
		String error,
		String message,
		String path) {

	public static ErrorResponseDto of(HttpStatus status, String message, String path) {
		return new ErrorResponseDto(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
	}
}
