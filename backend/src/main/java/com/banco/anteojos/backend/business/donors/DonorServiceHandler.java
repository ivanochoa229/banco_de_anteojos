package com.banco.anteojos.backend.business.donors;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.common.SearchTerms;
import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
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
				request.documentNumber(), request.phone(), request.email(), request.address(),
				request.postalCode(), request.city(), request.province()));
		return toResponse(donor);
	}

	@Override
	public DonorResponseDto getDonor(Long donorId) {
		return toResponse(donorRepository.findById(donorId).orElseThrow(DonorNotFoundException::new));
	}

	@Override
	public PageResponseDto<DonorResponseDto> listDonors(String search, Collection<Long> ids, Pageable pageable) {
		Page<Donor> donors = ids == null || ids.isEmpty()
				? donorRepository.search(SearchTerms.normalize(search), pageable)
				: donorRepository.findByIdInOrderByNameAscIdAsc(ids, pageable);
		return PageResponseDto.from(donors.map(this::toResponse));
	}

	@Override
	public Map<Long, String> getNames(Collection<Long> donorIds) {
		return donorRepository.findAllById(donorIds).stream().collect(Collectors.toMap(Donor::getId, Donor::getName));
	}

	private DonorResponseDto toResponse(Donor donor) {
		return new DonorResponseDto(donor.getId(), donor.getDonorType(), donor.getName(),
				donor.getDocumentNumber(), donor.getPhone(), donor.getEmail(), donor.getAddress(),
				donor.getPostalCode(), donor.getCity(), donor.getProvince(), donor.getCreatedAt());
	}
}
