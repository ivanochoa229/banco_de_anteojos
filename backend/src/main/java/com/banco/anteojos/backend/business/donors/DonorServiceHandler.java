package com.banco.anteojos.backend.business.donors;

import java.util.List;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.donors.dto.request.DonorCreationRequestDto;
import com.banco.anteojos.backend.business.donors.dto.response.DonorResponseDto;
import com.banco.anteojos.backend.business.donors.entities.Donor;
import com.banco.anteojos.backend.business.donors.exception.DonorNotFoundException;
import com.banco.anteojos.backend.persistence.donors.DonorPostgresSqlRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DonorServiceHandler implements DonorService {

	private final DonorPostgresSqlRepository donorRepository;

	@Override
	public DonorResponseDto createDonor(DonorCreationRequestDto request) {
		Donor donor = donorRepository.save(new Donor(request.donorType(), request.name(),
				request.documentNumber(), request.phone(), request.email()));
		return toResponse(donor);
	}

	@Override
	public DonorResponseDto getDonor(Long donorId) {
		return toResponse(donorRepository.findById(donorId).orElseThrow(DonorNotFoundException::new));
	}

	@Override
	public List<DonorResponseDto> listDonors() {
		return donorRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
	}

	private DonorResponseDto toResponse(Donor donor) {
		return new DonorResponseDto(donor.getId(), donor.getDonorType(), donor.getName(),
				donor.getDocumentNumber(), donor.getPhone(), donor.getEmail(), donor.getCreatedAt());
	}
}
