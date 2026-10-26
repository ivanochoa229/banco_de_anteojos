package com.banco.anteojos.backend.business.donors;

import java.util.Collection;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.business.donors.dto.request.DonorCreationRequestDto;
import com.banco.anteojos.backend.business.donors.dto.response.DonorResponseDto;

public interface DonorService {

	DonorResponseDto createDonor(DonorCreationRequestDto request);

	DonorResponseDto getDonor(Long donorId);

	/**
	 * Con {@code ids} trae esos donantes (para mostrar de quién vino cada marco de una página del
	 * inventario); si no, busca por nombre. {@code search} null o en blanco no filtra.
	 */
	PageResponseDto<DonorResponseDto> listDonors(String search, Collection<Long> ids, Pageable pageable);

	/** Nombre por id, para mostrar en listados de otros dominios. Los ids inexistentes no vienen. */
	Map<Long, String> getNames(Collection<Long> donorIds);
}
