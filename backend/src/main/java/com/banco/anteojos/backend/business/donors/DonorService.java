package com.banco.anteojos.backend.business.donors;

import java.util.List;

import com.banco.anteojos.backend.business.donors.dto.request.DonorCreationRequestDto;
import com.banco.anteojos.backend.business.donors.dto.response.DonorResponseDto;

public interface DonorService {

	DonorResponseDto createDonor(DonorCreationRequestDto request);

	DonorResponseDto getDonor(Long donorId);

	List<DonorResponseDto> listDonors();
}
