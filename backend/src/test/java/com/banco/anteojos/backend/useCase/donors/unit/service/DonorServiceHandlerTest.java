package com.banco.anteojos.backend.useCase.donors.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.business.donors.DonorServiceHandler;
import com.banco.anteojos.backend.business.donors.dto.request.DonorCreationRequestDto;
import com.banco.anteojos.backend.business.donors.dto.response.DonorResponseDto;
import com.banco.anteojos.backend.business.donors.entities.Donor;
import com.banco.anteojos.backend.business.donors.entities.DonorType;
import com.banco.anteojos.backend.business.donors.exception.DonorNotFoundException;
import com.banco.anteojos.backend.persistence.donors.DonorPostgresSqlRepository;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class DonorServiceHandlerTest {

	@Mock
	private DonorPostgresSqlRepository donorRepository;

	@InjectMocks
	private DonorServiceHandler donorServiceHandler;

	private static final Pageable PAGE = PageRequest.of(0, 20);

	@Test
	void CreateDonor_Successful() {
		when(donorRepository.save(any(Donor.class))).thenAnswer(inv -> inv.getArgument(0));

		DonorResponseDto response = donorServiceHandler.createDonor(new DonorCreationRequestDto(
				DonorType.ORGANIZATION, "  Óptica Central  ", "30712345678", "3814440000", "Contacto@Optica.COM",
				"  Av. Colón 1200  ", "X5000", "  Córdoba  ", "Córdoba"));

		assertThat(response.donorType()).isEqualTo(DonorType.ORGANIZATION);
		assertThat(response.name()).isEqualTo("Óptica Central");
		assertThat(response.email()).isEqualTo("contacto@optica.com");
		assertThat(response.address()).isEqualTo("Av. Colón 1200");
		assertThat(response.postalCode()).isEqualTo("X5000");
		assertThat(response.city()).isEqualTo("Córdoba");
		assertThat(response.province()).isEqualTo("Córdoba");
	}

	@Test
	void CreateDonor_WithoutOptionalData() {
		when(donorRepository.save(any(Donor.class))).thenAnswer(inv -> inv.getArgument(0));

		DonorResponseDto response = donorServiceHandler.createDonor(new DonorCreationRequestDto(
				DonorType.INDIVIDUAL, "Donante Anónimo", null, null, null, null, null, null, null));

		assertThat(response.documentNumber()).isNull();
		assertThat(response.phone()).isNull();
		assertThat(response.email()).isNull();
		assertThat(response.city()).isNull();
	}

	@Test
	void GetDonor_WhenNotFound() {
		when(donorRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> donorServiceHandler.getDonor(999L))
				.isInstanceOf(DonorNotFoundException.class);
	}

	@Test
	void ListDonors_Successful() {
		when(donorRepository.search("", PAGE)).thenReturn(new PageImpl<>(List.of(
				new Donor(DonorType.INDIVIDUAL, "Ana Ruiz", null, null, null, null, null, null, null),
				new Donor(DonorType.ORGANIZATION, "Rotary Club", "30711111119", null, null, null, null,
						null, null)), PAGE, 2));

		PageResponseDto<DonorResponseDto> response = donorServiceHandler.listDonors(null, null, PAGE);

		assertThat(response.content()).hasSize(2);
		assertThat(response.content().get(0).name()).isEqualTo("Ana Ruiz");
		assertThat(response.totalElements()).isEqualTo(2);
	}

	@Test
	void ListDonors_WhenSearchingSearchesWithoutAccentsOrCase() {
		when(donorRepository.search("optica munoz", PAGE)).thenReturn(new PageImpl<>(List.of(), PAGE, 0));

		donorServiceHandler.listDonors("  Óptica Muñoz ", List.of(), PAGE);

		verify(donorRepository).search("optica munoz", PAGE);
	}

	@Test
	void ListDonors_WhenIdsAreGiven() {
		when(donorRepository.findByIdInOrderByNameAscIdAsc(List.of(1L, 2L), PAGE))
				.thenReturn(new PageImpl<>(List.of(), PAGE, 0));

		donorServiceHandler.listDonors("ignorado", List.of(1L, 2L), PAGE);

		verify(donorRepository).findByIdInOrderByNameAscIdAsc(List.of(1L, 2L), PAGE);
		verify(donorRepository, never()).search(any(), any());
	}

	@Test
	void GetNames_Successful() {
		Donor donor = new Donor(DonorType.INDIVIDUAL, "Ana Ruiz", null, null, null, null, null, null, null);
		ReflectionTestUtils.setField(donor, "id", 4L);
		when(donorRepository.findAllById(Set.of(4L, 99L))).thenReturn(List.of(donor));

		assertThat(donorServiceHandler.getNames(Set.of(4L, 99L))).containsExactly(Map.entry(4L, "Ana Ruiz"));
	}
}
