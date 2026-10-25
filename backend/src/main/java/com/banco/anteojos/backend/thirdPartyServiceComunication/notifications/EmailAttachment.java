package com.banco.anteojos.backend.thirdPartyServiceComunication.notifications;

public record EmailAttachment(
		byte[] content,
		String contentType,
		String fileName) {
}
