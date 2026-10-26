package com.banco.anteojos.backend.orchestrator.donors;

import java.util.Collection;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.business.donors.DonorService;
import com.banco.anteojos.backend.business.donors.dto.request.DonorCreationRequestDto;
import com.banco.anteojos.backend.business.donors.dto.response.DonorResponseDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DonorUseCaseHandler implements DonorUseCaseOrchestrator {

	private final DonorService donorService;

	@Override
	public DonorResponseDto createDonor(DonorCreationRequestDto request) {
		return donorService.createDonor(request);
	}

	@Override
	public DonorResponseDto getDonor(Long donorId) {
		return donorService.getDonor(donorId);
	}

	@Override
	public PageResponseDto<DonorResponseDto> listDonors(String search, Collection<Long> ids, Pageable pageable) {
		return donorService.listDonors(search, ids, pageable);
	}
}
