package com.banco.anteojos.backend.business.appointments.dto.request;

/**
 * Archivo ya leído por el controller. Se pasa como bytes y no como MultipartFile para que la capa
 * de negocio no dependa de tipos web y el service sea unit-testeable sin mockear el request.
 */
public record AppointmentReceiptUploadRequestDto(
		byte[] content,
		String contentType,
		String originalName) {
}
