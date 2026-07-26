package com.banco.anteojos.backend.persistence.frames;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.anteojos.backend.business.frames.entities.Frame;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;

public interface FramePostgresSqlRepository extends JpaRepository<Frame, Long> {

	Optional<Frame> findBySealCode(String sealCode);

	List<Frame> findAllByOrderByReceivedAtDesc();

	List<Frame> findByStatusOrderByReceivedAtDesc(FrameStatus status);

	List<Frame> findByDonorIdOrderByReceivedAtDesc(Long donorId);
}
