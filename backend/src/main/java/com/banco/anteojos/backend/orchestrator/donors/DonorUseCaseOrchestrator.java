package com.banco.anteojos.backend.orchestrator.donors;

import java.util.Collection;

import org.springframework.data.domain.Pageable;

import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.business.donors.dto.request.DonorCreationRequestDto;
import com.banco.anteojos.backend.business.donors.dto.response.DonorResponseDto;

public interface DonorUseCaseOrchestrator {

	DonorResponseDto createDonor(DonorCreationRequestDto request);

	DonorResponseDto getDonor(Long donorId);

	PageResponseDto<DonorResponseDto> listDonors(String search, Collection<Long> ids, Pageable pageable);
}
