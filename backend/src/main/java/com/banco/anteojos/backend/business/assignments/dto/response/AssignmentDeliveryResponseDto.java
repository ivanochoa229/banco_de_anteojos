package com.banco.anteojos.backend.business.assignments.dto.response;

import com.banco.anteojos.backend.thirdPartyServiceComunication.notifications.EmailAttachment;

/**
 * Resultado interno de registrar la entrega, no sale por la API.
 *
 * @param photoForDonor la foto para adjuntarle al donante, o null si no hay foto o el
 *        beneficiario no autorizó compartirla. La decisión la toma el service.
 */
public record AssignmentDeliveryResponseDto(
		AssignmentResponseDto assignment,
		EmailAttachment photoForDonor) {
}
