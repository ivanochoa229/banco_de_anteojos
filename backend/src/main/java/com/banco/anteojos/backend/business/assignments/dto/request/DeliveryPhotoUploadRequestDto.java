package com.banco.anteojos.backend.business.assignments.dto.request;

public record DeliveryPhotoUploadRequestDto(
		byte[] content,
		String contentType,
		String originalName) {
}
