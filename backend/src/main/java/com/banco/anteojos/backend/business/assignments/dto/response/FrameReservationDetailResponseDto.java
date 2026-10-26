package com.banco.anteojos.backend.business.assignments.dto.response;

import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;

/** La reserva con el marco reservado, para mostrarlo sin un segundo pedido. */
public record FrameReservationDetailResponseDto(
		FrameReservationResponseDto reservation,
		FrameResponseDto frame) {
}
