package com.banco.anteojos.backend.presentation.error;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.banco.anteojos.backend.business.applicants.exception.AnsesCertificateNotFoundException;
import com.banco.anteojos.backend.business.applicants.exception.ApplicantNotFoundException;
import com.banco.anteojos.backend.business.applicants.exception.DniAlreadyExistsException;
import com.banco.anteojos.backend.business.applicants.exception.InvalidAnsesCertificateException;
import com.banco.anteojos.backend.business.applicants.exception.InvalidPrescriptionFileException;
import com.banco.anteojos.backend.business.applicants.exception.PrescriptionFileNotFoundException;
import com.banco.anteojos.backend.business.applicants.exception.PrescriptionNotFoundException;
import com.banco.anteojos.backend.business.appointments.exception.AppointmentNotFoundException;
import com.banco.anteojos.backend.business.appointments.exception.InvalidAppointmentTransitionException;
import com.banco.anteojos.backend.business.assignments.exception.AssignmentNotFoundException;
import com.banco.anteojos.backend.business.assignments.exception.InvalidAssignmentTransitionException;
import com.banco.anteojos.backend.business.donors.exception.DonorNotFoundException;
import com.banco.anteojos.backend.business.frames.exception.FrameImageNotFoundException;
import com.banco.anteojos.backend.business.frames.exception.InvalidFrameImageException;
import com.banco.anteojos.backend.business.frames.exception.FrameNotFoundException;
import com.banco.anteojos.backend.business.frames.exception.InvalidFrameTransitionException;
import com.banco.anteojos.backend.business.frames.exception.SealCodeAlreadyExistsException;
import com.banco.anteojos.backend.business.security.exception.InvalidCredentialsException;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.StorageException;

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

	@ExceptionHandler(FrameImageNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponseDto frameImageNotFound(FrameImageNotFoundException e, HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(InvalidFrameImageException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponseDto invalidFrameImage(InvalidFrameImageException e, HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.BAD_REQUEST, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(PrescriptionNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponseDto prescriptionNotFound(PrescriptionNotFoundException e, HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(PrescriptionFileNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponseDto prescriptionFileNotFound(PrescriptionFileNotFoundException e,
			HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(InvalidPrescriptionFileException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponseDto invalidPrescriptionFile(InvalidPrescriptionFileException e,
			HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.BAD_REQUEST, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(AssignmentNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponseDto assignmentNotFound(AssignmentNotFoundException e, HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
	}

	// 409 y no 400: el request está bien formado, lo que no encaja es el momento del circuito
	// en el que está el marco o la asignación, o el estado del turno.
	@ExceptionHandler({ InvalidAssignmentTransitionException.class, InvalidFrameTransitionException.class,
			InvalidAppointmentTransitionException.class })
	@ResponseStatus(HttpStatus.CONFLICT)
	public ErrorResponseDto invalidTransition(RuntimeException e, HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.CONFLICT, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(AppointmentNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponseDto appointmentNotFound(AppointmentNotFoundException e, HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(AnsesCertificateNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ErrorResponseDto ansesCertificateNotFound(AnsesCertificateNotFoundException e,
			HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
	}

	// 422 y no 400: el archivo llegó bien pero el documento no pasa la validación de negocio
	// (ilegible, de otra persona o vencido). No se persiste nada.
	@ExceptionHandler(InvalidAnsesCertificateException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
	public ErrorResponseDto invalidAnsesCertificate(InvalidAnsesCertificateException e,
			HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage(), request.getRequestURI());
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	@ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
	public ErrorResponseDto fileTooLarge(HttpServletRequest request) {
		return ErrorResponseDto.of(HttpStatus.CONTENT_TOO_LARGE, "El archivo supera los 10 MB",
				request.getRequestURI());
	}

	// El detalle del fallo de R2 va al log; al operador solo le sirve saber que reintente.
	@ExceptionHandler(StorageException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public ErrorResponseDto storageUnavailable(StorageException e, HttpServletRequest request) {
		log.error("Fallo de almacenamiento en {}", request.getRequestURI(), e);
		return ErrorResponseDto.of(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), request.getRequestURI());
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
