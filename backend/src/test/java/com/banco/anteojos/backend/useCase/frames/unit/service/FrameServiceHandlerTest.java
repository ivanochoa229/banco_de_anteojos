package com.banco.anteojos.backend.useCase.frames.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banco.anteojos.backend.business.frames.FrameServiceHandler;
import com.banco.anteojos.backend.business.frames.dto.request.FrameCreationRequestDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.frames.entities.Frame;
import com.banco.anteojos.backend.business.frames.entities.FrameMaterial;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;
import com.banco.anteojos.backend.business.frames.entities.FrameType;
import com.banco.anteojos.backend.business.frames.exception.FrameNotFoundException;
import com.banco.anteojos.backend.business.frames.exception.SealCodeAlreadyExistsException;
import com.banco.anteojos.backend.persistence.frames.FramePostgresSqlRepository;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class FrameServiceHandlerTest {

	@Mock
	private FramePostgresSqlRepository frameRepository;

	@InjectMocks
	private FrameServiceHandler frameServiceHandler;

	private Frame frame(String sealCode) {
		return new Frame(1L, sealCode, FrameType.FULL_RIM, FrameMaterial.ACETATE, 52, 18, 140);
	}

	@Test
	void CreateFrame_Successful() {
		when(frameRepository.findBySealCode("A-1001")).thenReturn(Optional.empty());
		when(frameRepository.save(any(Frame.class))).thenAnswer(inv -> inv.getArgument(0));

		FrameResponseDto response = frameServiceHandler.createFrame(1L, new FrameCreationRequestDto(
				"a-1001", FrameType.FULL_RIM, FrameMaterial.ACETATE, 52, 18, 140));

		assertThat(response.donorId()).isEqualTo(1L);
		assertThat(response.lensWidthMm()).isEqualTo(52);
		// Nace disponible: el resto de las transiciones las maneja assignments.
		assertThat(response.status()).isEqualTo(FrameStatus.AVAILABLE);
	}

	@Test
	void CreateFrame_NormalizesSealCode() {
		when(frameRepository.findBySealCode("A-1001")).thenReturn(Optional.empty());
		when(frameRepository.save(any(Frame.class))).thenAnswer(inv -> inv.getArgument(0));

		FrameResponseDto response = frameServiceHandler.createFrame(1L, new FrameCreationRequestDto(
				"  a-1001  ", FrameType.RIMLESS, FrameMaterial.TITANIUM, null, null, null));

		assertThat(response.sealCode()).isEqualTo("A-1001");
	}

	@Test
	void CreateFrame_WhenSealCodeAlreadyExists() {
		when(frameRepository.findBySealCode("A-1001")).thenReturn(Optional.of(frame("A-1001")));

		assertThatThrownBy(() -> frameServiceHandler.createFrame(1L, new FrameCreationRequestDto(
				"A-1001", FrameType.FULL_RIM, FrameMaterial.METAL, null, null, null)))
				.isInstanceOf(SealCodeAlreadyExistsException.class);
		verify(frameRepository, never()).save(any());
	}

	@Test
	void GetFrame_WhenNotFound() {
		when(frameRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> frameServiceHandler.getFrame(999L))
				.isInstanceOf(FrameNotFoundException.class);
	}

	@Test
	void ListFrames_WithoutStatusReturnsWholeInventory() {
		when(frameRepository.findAllByOrderByReceivedAtDesc())
				.thenReturn(List.of(frame("A-1001"), frame("A-1002")));

		List<FrameResponseDto> response = frameServiceHandler.listFrames(null);

		assertThat(response).hasSize(2);
		verify(frameRepository, never()).findByStatusOrderByReceivedAtDesc(any());
	}

	@Test
	void ListFrames_FilteredByStatus() {
		when(frameRepository.findByStatusOrderByReceivedAtDesc(FrameStatus.AVAILABLE))
				.thenReturn(List.of(frame("A-1001")));

		List<FrameResponseDto> response = frameServiceHandler.listFrames(FrameStatus.AVAILABLE);

		assertThat(response).hasSize(1);
		verify(frameRepository, never()).findAllByOrderByReceivedAtDesc();
	}

	@Test
	void ListFramesByDonor_Successful() {
		when(frameRepository.findByDonorIdOrderByReceivedAtDesc(1L)).thenReturn(List.of(frame("A-1001")));

		List<FrameResponseDto> response = frameServiceHandler.listFramesByDonor(1L);

		assertThat(response).hasSize(1);
		assertThat(response.get(0).sealCode()).isEqualTo("A-1001");
	}
}
