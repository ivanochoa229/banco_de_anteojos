package com.banco.anteojos.backend.business.frames;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.frames.dto.request.FrameCreationRequestDto;
import com.banco.anteojos.backend.business.frames.dto.request.FrameImageUploadRequestDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameImageResponseDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.frames.entities.Frame;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;
import com.banco.anteojos.backend.business.frames.exception.FrameImageNotFoundException;
import com.banco.anteojos.backend.business.frames.exception.FrameNotFoundException;
import com.banco.anteojos.backend.business.frames.exception.InvalidFrameImageException;
import com.banco.anteojos.backend.business.frames.exception.SealCodeAlreadyExistsException;
import com.banco.anteojos.backend.persistence.frames.FramePostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.PresignedUrl;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FrameServiceHandler implements FrameService {

	/**
	 * Solo formatos con canal alfa. El probador dibuja la foto del marco encima de la cara: un
	 * JPG no tiene transparencia y se superpondría como un rectángulo opaco tapando media cara.
	 */
	private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
			"image/png", "png",
			"image/webp", "webp");

	private final FramePostgresSqlRepository frameRepository;
	private final R2StorageClient r2StorageClient;

	@Override
	public FrameResponseDto createFrame(Long donorId, FrameCreationRequestDto request) {
		String sealCode = Frame.normalizeSealCode(request.sealCode());
		if (frameRepository.findBySealCode(sealCode).isPresent()) {
			throw new SealCodeAlreadyExistsException();
		}
		Frame frame = frameRepository.save(new Frame(donorId, sealCode, request.frameType(),
				request.material(), request.lensWidthMm(), request.bridgeWidthMm(), request.templeLengthMm()));
		return toResponse(frame);
	}

	@Override
	public FrameResponseDto getFrame(Long frameId) {
		return toResponse(findFrame(frameId));
	}

	@Override
	public List<FrameResponseDto> getFrames(List<Long> frameIds) {
		List<Frame> frames = frameRepository.findAllById(frameIds);
		// findAllById ignora en silencio los ids inexistentes: la diferencia de tamaño los delata.
		if (frames.size() != Set.copyOf(frameIds).size()) {
			throw new FrameNotFoundException();
		}
		return frames.stream().map(this::toResponse).toList();
	}

	@Override
	public FrameResponseDto uploadFrameImage(Long frameId, FrameImageUploadRequestDto request) {
		Frame frame = findFrame(frameId);
		String extension = validateImage(request);

		String key = "frames/%d/%s.%s".formatted(frameId, UUID.randomUUID(), extension);
		r2StorageClient.upload(key, request.content(), request.contentType());

		String previousKey = frame.attachImage(key, request.contentType(), request.originalName());
		FrameResponseDto response = toResponse(frameRepository.save(frame));

		// Limpiar la foto reemplazada no es crítico: si falla queda un huérfano en R2, pero el
		// marco ya apunta a la nueva y el operador no tiene por qué enterarse.
		if (previousKey != null) {
			try {
				r2StorageClient.delete(previousKey);
			} catch (RuntimeException e) {
				log.warn("No se pudo borrar la foto reemplazada {} del marco {}", previousKey, frameId, e);
			}
		}
		return response;
	}

	@Override
	public FrameImageResponseDto getFrameImage(Long frameId) {
		Frame frame = findFrame(frameId);
		if (!frame.hasImage()) {
			throw new FrameImageNotFoundException();
		}
		PresignedUrl presigned = r2StorageClient.presignedGetUrl(frame.getImageKey());
		return new FrameImageResponseDto(presigned.url(), presigned.expiresAt(),
				frame.getImageOriginalName(), frame.getImageContentType());
	}

	/** Devuelve la extensión que corresponde al content type, ya validado. */
	private String validateImage(FrameImageUploadRequestDto request) {
		if (request.content() == null || request.content().length == 0) {
			throw new InvalidFrameImageException("El archivo está vacío");
		}
		String extension = ALLOWED_IMAGE_TYPES.get(request.contentType());
		if (extension == null) {
			throw new InvalidFrameImageException(
					"La foto del marco debe ser PNG o WEBP con el fondo recortado: un JPG no tiene "
							+ "transparencia y en el probador se vería como un recuadro sobre la cara");
		}
		return extension;
	}

	@Override
	public List<FrameResponseDto> listFrames(FrameStatus status) {
		List<Frame> frames = status == null
				? frameRepository.findAllByOrderByReceivedAtDesc()
				: frameRepository.findByStatusOrderByReceivedAtDesc(status);
		return frames.stream().map(this::toResponse).toList();
	}

	@Override
	public List<FrameResponseDto> listFramesByDonor(Long donorId) {
		return frameRepository.findByDonorIdOrderByReceivedAtDesc(donorId).stream().map(this::toResponse).toList();
	}

	@Override
	public long countReceived(LocalDateTime from, LocalDateTime to) {
		return frameRepository.countByReceivedAtBetween(from, to);
	}

	@Override
	public FrameResponseDto markAsAssigned(Long frameId) {
		return transition(frameId, Frame::markAsAssigned);
	}

	@Override
	public FrameResponseDto markAsAtOptician(Long frameId) {
		return transition(frameId, Frame::markAsAtOptician);
	}

	@Override
	public FrameResponseDto markAsReady(Long frameId) {
		return transition(frameId, Frame::markAsReady);
	}

	@Override
	public FrameResponseDto markAsDelivered(Long frameId) {
		return transition(frameId, Frame::markAsDelivered);
	}

	@Override
	public FrameResponseDto returnToInventory(Long frameId) {
		return transition(frameId, Frame::returnToInventory);
	}

	/** Cada transición valida su estado de origen dentro de la entidad y falla si no encaja. */
	private FrameResponseDto transition(Long frameId, Consumer<Frame> transition) {
		Frame frame = findFrame(frameId);
		transition.accept(frame);
		return toResponse(frameRepository.save(frame));
	}

	private Frame findFrame(Long frameId) {
		return frameRepository.findById(frameId).orElseThrow(FrameNotFoundException::new);
	}

	private FrameResponseDto toResponse(Frame frame) {
		return new FrameResponseDto(frame.getId(), frame.getDonorId(), frame.getSealCode(),
				frame.getFrameType(), frame.getMaterial(), frame.getLensWidthMm(), frame.getBridgeWidthMm(),
				frame.getTempleLengthMm(), frame.getStatus(), frame.getImageOriginalName(),
				frame.getReceivedAt());
	}
}
