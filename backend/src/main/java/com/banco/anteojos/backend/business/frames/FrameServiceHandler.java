package com.banco.anteojos.backend.business.frames;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.frames.dto.request.FrameCreationRequestDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.frames.entities.Frame;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;
import com.banco.anteojos.backend.business.frames.exception.FrameNotFoundException;
import com.banco.anteojos.backend.business.frames.exception.SealCodeAlreadyExistsException;
import com.banco.anteojos.backend.persistence.frames.FramePostgresSqlRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FrameServiceHandler implements FrameService {

	private final FramePostgresSqlRepository frameRepository;

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
				frame.getTempleLengthMm(), frame.getStatus(), frame.getReceivedAt());
	}
}
