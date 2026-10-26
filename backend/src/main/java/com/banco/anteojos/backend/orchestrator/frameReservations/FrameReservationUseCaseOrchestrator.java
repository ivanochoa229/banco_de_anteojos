package com.banco.anteojos.backend.orchestrator.frameReservations;

import com.banco.anteojos.backend.business.assignments.dto.response.FrameReservationDetailResponseDto;

public interface FrameReservationUseCaseOrchestrator {

	FrameReservationDetailResponseDto reserve(Long applicantId, Long frameId);

	FrameReservationDetailResponseDto getReservation(Long applicantId);

	void cancelReservation(Long applicantId);

	/** Lo dispara la tarea programada: libera los marcos de las reservas vencidas. */
	void expireOverdueReservations();
}
