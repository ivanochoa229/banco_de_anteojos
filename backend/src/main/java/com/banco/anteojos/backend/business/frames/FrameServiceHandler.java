package com.banco.anteojos.backend.business.frames;

import java.util.List;

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
		return toResponse(frameRepository.findById(frameId).orElseThrow(FrameNotFoundException::new));
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

	private FrameResponseDto toResponse(Frame frame) {
		return new FrameResponseDto(frame.getId(), frame.getDonorId(), frame.getSealCode(),
				frame.getFrameType(), frame.getMaterial(), frame.getLensWidthMm(), frame.getBridgeWidthMm(),
				frame.getTempleLengthMm(), frame.getStatus(), frame.getReceivedAt());
	}
}
