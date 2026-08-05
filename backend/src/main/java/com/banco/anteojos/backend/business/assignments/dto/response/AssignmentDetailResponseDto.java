package com.banco.anteojos.backend.business.assignments.dto.response;

import com.banco.anteojos.backend.business.applicants.dto.response.ApplicantResponseDto;
import com.banco.anteojos.backend.business.donors.dto.response.DonorResponseDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;

/**
 * Trazabilidad end-to-end de un par de anteojos (RF-16): quién donó el marco, a quién se le
 * asignó y las fechas de cada paso del circuito, en un solo request.
 */
public record AssignmentDetailResponseDto(
		AssignmentResponseDto assignment,
		ApplicantResponseDto applicant,
		FrameResponseDto frame,
		DonorResponseDto donor) {
}
