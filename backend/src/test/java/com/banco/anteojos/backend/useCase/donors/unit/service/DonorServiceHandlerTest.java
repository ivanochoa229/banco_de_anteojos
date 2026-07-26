package com.banco.anteojos.backend.useCase.donors.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

	@Test
	void CreateDonor_Successful() {
		when(donorRepository.save(any(Donor.class))).thenAnswer(inv -> inv.getArgument(0));

		DonorResponseDto response = donorServiceHandler.createDonor(new DonorCreationRequestDto(
				DonorType.ORGANIZATION, "  Óptica Central  ", "30712345678", "3814440000", "Contacto@Optica.COM"));

		assertThat(response.donorType()).isEqualTo(DonorType.ORGANIZATION);
		assertThat(response.name()).isEqualTo("Óptica Central");
		assertThat(response.email()).isEqualTo("contacto@optica.com");
	}

	@Test
	void CreateDonor_WithoutOptionalData() {
		when(donorRepository.save(any(Donor.class))).thenAnswer(inv -> inv.getArgument(0));

		DonorResponseDto response = donorServiceHandler.createDonor(new DonorCreationRequestDto(
				DonorType.INDIVIDUAL, "Donante Anónimo", null, null, null));

		assertThat(response.documentNumber()).isNull();
		assertThat(response.phone()).isNull();
		assertThat(response.email()).isNull();
	}

	@Test
	void GetDonor_WhenNotFound() {
		when(donorRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> donorServiceHandler.getDonor(999L))
				.isInstanceOf(DonorNotFoundException.class);
	}

	@Test
	void ListDonors_Successful() {
		when(donorRepository.findAllByOrderByNameAsc()).thenReturn(List.of(
				new Donor(DonorType.INDIVIDUAL, "Ana Ruiz", null, null, null),
				new Donor(DonorType.ORGANIZATION, "Rotary Club", "30711111119", null, null)));

		List<DonorResponseDto> response = donorServiceHandler.listDonors();

		assertThat(response).hasSize(2);
		assertThat(response.get(0).name()).isEqualTo("Ana Ruiz");
	}
}
