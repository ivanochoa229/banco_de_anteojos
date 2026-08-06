package com.banco.anteojos.backend.useCase.frames.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import com.banco.anteojos.backend.business.frames.dto.request.FrameImageUploadRequestDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameImageResponseDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.frames.entities.Frame;
import com.banco.anteojos.backend.business.frames.entities.FrameMaterial;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;
import com.banco.anteojos.backend.business.frames.entities.FrameType;
import com.banco.anteojos.backend.business.frames.exception.FrameImageNotFoundException;
import com.banco.anteojos.backend.business.frames.exception.FrameNotFoundException;
import com.banco.anteojos.backend.business.frames.exception.InvalidFrameImageException;
import com.banco.anteojos.backend.business.frames.exception.InvalidFrameTransitionException;
import com.banco.anteojos.backend.business.frames.exception.SealCodeAlreadyExistsException;
import com.banco.anteojos.backend.persistence.frames.FramePostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class FrameServiceHandlerTest {

	@Mock
	private FramePostgresSqlRepository frameRepository;

	@Mock
	private R2StorageClient r2StorageClient;

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

	/** Deja el marco en el estado pedido recorriendo el circuito desde AVAILABLE. */
	private Frame frameAt(FrameStatus status) {
		Frame frame = frame("A-1001");
		if (status == FrameStatus.AVAILABLE) {
			return frame;
		}
		frame.markAsAssigned();
		if (status == FrameStatus.ASSIGNED) {
			return frame;
		}
		frame.markAsAtOptician();
		if (status == FrameStatus.AT_OPTICIAN) {
			return frame;
		}
		frame.markAsReady();
		if (status == FrameStatus.READY) {
			return frame;
		}
		frame.markAsDelivered();
		return frame;
	}

	private void mockFind(Frame frame) {
		when(frameRepository.findById(1L)).thenReturn(Optional.of(frame));
		when(frameRepository.save(any(Frame.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	void MarkAsAssigned_Successful() {
		mockFind(frameAt(FrameStatus.AVAILABLE));

		assertThat(frameServiceHandler.markAsAssigned(1L).status()).isEqualTo(FrameStatus.ASSIGNED);
	}

	@Test
	void MarkAsAssigned_WhenFrameIsNotAvailable() {
		when(frameRepository.findById(1L)).thenReturn(Optional.of(frameAt(FrameStatus.ASSIGNED)));

		// Es el chequeo que evita que dos beneficiarios se lleven el mismo marco.
		assertThatThrownBy(() -> frameServiceHandler.markAsAssigned(1L))
				.isInstanceOf(InvalidFrameTransitionException.class)
				.hasMessageContaining("no está disponible");
		verify(frameRepository, never()).save(any());
	}

	@Test
	void MarkAsAssigned_WhenFrameDoesNotExist() {
		when(frameRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> frameServiceHandler.markAsAssigned(999L))
				.isInstanceOf(FrameNotFoundException.class);
	}

	@Test
	void MarkAsAtOptician_Successful() {
		mockFind(frameAt(FrameStatus.ASSIGNED));

		assertThat(frameServiceHandler.markAsAtOptician(1L).status()).isEqualTo(FrameStatus.AT_OPTICIAN);
	}

	@Test
	void MarkAsReady_Successful() {
		mockFind(frameAt(FrameStatus.AT_OPTICIAN));

		assertThat(frameServiceHandler.markAsReady(1L).status()).isEqualTo(FrameStatus.READY);
	}

	@Test
	void MarkAsDelivered_Successful() {
		mockFind(frameAt(FrameStatus.READY));

		assertThat(frameServiceHandler.markAsDelivered(1L).status()).isEqualTo(FrameStatus.DELIVERED);
	}

	@Test
	void MarkAsDelivered_WhenFrameIsStillAtOptician() {
		when(frameRepository.findById(1L)).thenReturn(Optional.of(frameAt(FrameStatus.AT_OPTICIAN)));

		assertThatThrownBy(() -> frameServiceHandler.markAsDelivered(1L))
				.isInstanceOf(InvalidFrameTransitionException.class)
				.hasMessageContaining("cristales");
	}

	@Test
	void ReturnToInventory_Successful() {
		mockFind(frameAt(FrameStatus.AT_OPTICIAN));

		// Cancelar la asignación devuelve el marco al stock desde cualquier punto del circuito.
		assertThat(frameServiceHandler.returnToInventory(1L).status()).isEqualTo(FrameStatus.AVAILABLE);
	}

	@Test
	void ReturnToInventory_WhenFrameWasAlreadyDelivered() {
		when(frameRepository.findById(1L)).thenReturn(Optional.of(frameAt(FrameStatus.DELIVERED)));

		assertThatThrownBy(() -> frameServiceHandler.returnToInventory(1L))
				.isInstanceOf(InvalidFrameTransitionException.class)
				.hasMessageContaining("no vuelve al inventario");
	}

	// --- Foto del marco para el probador virtual (RF-18) ---

	private FrameImageUploadRequestDto imageRequest(String contentType) {
		return new FrameImageUploadRequestDto(new byte[] { 1, 2, 3 }, contentType, "marco.png");
	}

	@Test
	void UploadFrameImage_Successful() {
		mockFind(frame("A-1001"));

		FrameResponseDto response = frameServiceHandler.uploadFrameImage(1L, imageRequest("image/png"));

		assertThat(response.imageOriginalName()).isEqualTo("marco.png");
		// La key va prefijada por marco para poder mover el prefijo de bucket más adelante.
		verify(r2StorageClient).upload(startsWith("frames/1/"), any(), eq("image/png"));
	}

	@Test
	void UploadFrameImage_WhenFileIsJpeg() {
		when(frameRepository.findById(1L)).thenReturn(Optional.of(frame("A-1001")));

		// Sin canal alfa el marco taparía media cara: se rechaza antes de subirlo a R2.
		assertThatThrownBy(() -> frameServiceHandler.uploadFrameImage(1L, imageRequest("image/jpeg")))
				.isInstanceOf(InvalidFrameImageException.class)
				.hasMessageContaining("PNG o WEBP");
		verify(r2StorageClient, never()).upload(any(), any(), any());
	}

	@Test
	void UploadFrameImage_WhenFileIsEmpty() {
		when(frameRepository.findById(1L)).thenReturn(Optional.of(frame("A-1001")));

		assertThatThrownBy(() -> frameServiceHandler.uploadFrameImage(1L,
				new FrameImageUploadRequestDto(new byte[0], "image/png", "marco.png")))
				.isInstanceOf(InvalidFrameImageException.class)
				.hasMessageContaining("vacío");
		verify(r2StorageClient, never()).upload(any(), any(), any());
	}

	@Test
	void UploadFrameImage_ReplacesPreviousImage() {
		Frame frame = frame("A-1001");
		frame.attachImage("frames/1/vieja.png", "image/png", "vieja.png");
		mockFind(frame);

		frameServiceHandler.uploadFrameImage(1L, imageRequest("image/png"));

		verify(r2StorageClient).delete("frames/1/vieja.png");
	}

	@Test
	void UploadFrameImage_WhenDeletingPreviousImageFails() {
		Frame frame = frame("A-1001");
		frame.attachImage("frames/1/vieja.png", "image/png", "vieja.png");
		mockFind(frame);
		doThrow(new RuntimeException("R2 caído")).when(r2StorageClient).delete(any());

		// Borrar la foto vieja no es crítico: queda huérfana en R2 pero el marco ya apunta a la nueva.
		assertThat(frameServiceHandler.uploadFrameImage(1L, imageRequest("image/png"))
				.imageOriginalName()).isEqualTo("marco.png");
	}

	@Test
	void GetFrameImage_Successful() {
		Frame frame = frame("A-1001");
		frame.attachImage("frames/1/foto.png", "image/png", "foto.png");
		when(frameRepository.findById(1L)).thenReturn(Optional.of(frame));
		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
		when(r2StorageClient.presignedGetUrl("frames/1/foto.png"))
				.thenReturn(new PresignedUrl("https://r2/firmada", expiresAt));

		FrameImageResponseDto response = frameServiceHandler.getFrameImage(1L);

		assertThat(response.url()).isEqualTo("https://r2/firmada");
		assertThat(response.expiresAt()).isEqualTo(expiresAt);
		assertThat(response.fileName()).isEqualTo("foto.png");
	}

	@Test
	void GetFrameImage_WhenFrameHasNoImage() {
		when(frameRepository.findById(1L)).thenReturn(Optional.of(frame("A-1001")));

		assertThatThrownBy(() -> frameServiceHandler.getFrameImage(1L))
				.isInstanceOf(FrameImageNotFoundException.class);
	}
}
