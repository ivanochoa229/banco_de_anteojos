package com.banco.anteojos.backend.business.frames;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.business.frames.dto.request.FrameCreationRequestDto;
import com.banco.anteojos.backend.business.frames.dto.request.FrameImageUploadRequestDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameImageResponseDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;

public interface FrameService {

	FrameResponseDto createFrame(Long donorId, FrameCreationRequestDto request);

	FrameResponseDto getFrame(Long frameId);

	/** Corrige atributos cargados a mano. Falla si el precinto nuevo ya lo tiene otro marco. */
	FrameResponseDto updateFrame(Long frameId, FrameCreationRequestDto request);

	/** Baja definitiva (rotura, deterioro). Solo desde el inventario disponible. */
	FrameResponseDto discardFrame(Long frameId);

	/** Los marcos de un envío entre sucursales. Falla si alguno no existe en el inventario. */
	List<FrameResponseDto> getFrames(List<Long> frameIds);

	/** Foto recortada del marco para el probador virtual (RF-18). Subir otra reemplaza la anterior. */
	FrameResponseDto uploadFrameImage(Long frameId, FrameImageUploadRequestDto request);

	FrameImageResponseDto getFrameImage(Long frameId);

	/**
	 * Con status en null no filtra por estado. {@code search} busca en el precinto (null o en
	 * blanco no filtra). Con {@code withImage}, solo los que tienen foto para el probador.
	 */
	PageResponseDto<FrameResponseDto> listFrames(FrameStatus status, String search, boolean withImage,
			Pageable pageable);

	/** Con status en null no filtra por estado. */
	PageResponseDto<FrameResponseDto> listFramesByDonor(Long donorId, FrameStatus status, Pageable pageable);

	/** Precinto por id, para mostrar en listados de otros dominios. Los ids inexistentes no vienen. */
	Map<Long, String> getSealCodes(Collection<Long> frameIds);

	/** Donaciones recibidas en el rango, para el panel de indicadores (RF-26). */
	long countReceived(LocalDateTime from, LocalDateTime to);

	// Transiciones del ciclo de vida. Las dispara el dominio assignments a través del
	// orchestrator: acá solo se valida el estado de origen y se mueve el marco.

	/**
	 * Asigna el marco al beneficiario. Si lo tenía reservado, la reserva se concreta; si había
	 * reservado otro, ese otro vuelve al inventario.
	 *
	 * @param applicantReservedFrameId el marco que el beneficiario tenía reservado, o null.
	 */
	FrameResponseDto markAsAssigned(Long frameId, Long applicantReservedFrameId);

	/** El beneficiario lo eligió en el probador. Solo desde el inventario disponible. */
	FrameResponseDto reserve(Long frameId);

	/** Reservas canceladas o vencidas: los marcos vuelven al inventario. */
	void releaseReservations(Collection<Long> frameIds);

	FrameResponseDto markAsAtOptician(Long frameId);

	FrameResponseDto markAsReady(Long frameId);

	FrameResponseDto markAsDelivered(Long frameId);

	FrameResponseDto returnToInventory(Long frameId);
}
