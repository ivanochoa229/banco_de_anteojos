package com.banco.anteojos.backend.presentation.error;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.banco.anteojos.backend.business.applicants.exception.ApplicantNotFoundException;
import com.banco.anteojos.backend.business.applicants.exception.DniAlreadyExistsException;
import com.banco.anteojos.backend.business.donors.exception.DonorNotFoundException;
import com.banco.anteojos.backend.business.frames.exception.FrameNotFoundException;
import com.banco.anteojos.backend.business.frames.exception.SealCodeAlreadyExistsException;
import com.banco.anteojos.backend.business.security.exception.InvalidCredentialsException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ErrorResponseDto invalidCredentials(InvalidCredentialsException e, HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.UNAUTHORIZED, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(ApplicantNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponseDto applicantNotFound(ApplicantNotFoundException e, HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(DniAlreadyExistsException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponseDto dniAlreadyExists(DniAlreadyExistsException e, HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.CONFLICT, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(DonorNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponseDto donorNotFound(DonorNotFoundException e, HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(FrameNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponseDto frameNotFound(FrameNotFoundException e, HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(SealCodeAlreadyExistsException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponseDto sealCodeAlreadyExists(SealCodeAlreadyExistsException e, HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.CONFLICT, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponseDto validationError(MethodArgumentNotValidException e, HttpServletRequest request) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
				.sorted()
				.collect(Collectors.joining("; "));
		return ErrorResponseDto.of(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponseDto unreadableBody(HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.BAD_REQUEST, "Cuerpo de la petición inválido", request.getRequestURI());
	}

	// Nunca filtrar detalles internos (stacktrace, SQL) en la respuesta: van al log.
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponseDto internalError(Exception e, HttpServletRequest request) {
		log.error("Error no manejado en {}", request.getRequestURI(), e);
		return ErrorResponseDto.of(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor",
				request.getRequestURI());
	}
}
