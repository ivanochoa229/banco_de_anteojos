package com.banco.anteojos.backend.business.assignments;

import java.util.List;

import com.banco.anteojos.backend.business.applicants.dto.response.EligibilityResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.FrameReservationDetailResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.FrameReservationResponseDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;

public interface FrameReservationService {

	/**
	 * Falla si el beneficiario todavía no puede reservar. Va separado de {@link #reserve} para que
	 * el orchestrator valide todo antes de escribir nada.
	 *
	 * @param eligibility la negativa de ANSES, resuelta en applicants: tiene que estar vigente.
	 * @param hasPrescription si cargó al menos una receta: sin ella no se le puede asignar el marco.
	 */
	void validateCanReserve(Long applicantId, EligibilityResponseDto eligibility, boolean hasPrescription);

	/** Registra la reserva. Mover el marco a RESERVED es del dominio frames y lo coordina el orchestrator. */
	FrameReservationResponseDto reserve(Long applicantId, Long frameId);

	FrameReservationResponseDto getActive(Long applicantId);

	FrameReservationResponseDto cancelActive(Long applicantId);

	/**
	 * Cierra la reserva activa del beneficiario al asignarle un marco: se concreta si es el que
	 * reservó y se cancela si el operador eligió otro.
	 *
	 * @return el marco que tenía reservado (para que frames lo asigne o lo libere), o null.
	 */
	Long closeForAssignment(Long applicantId, Long assignedFrameId);

	/** Vence las reservas con el plazo cumplido y devuelve sus marcos, para liberarlos. */
	List<Long> expireOverdue();

	FrameReservationDetailResponseDto buildDetail(FrameReservationResponseDto reservation, FrameResponseDto frame);
}
