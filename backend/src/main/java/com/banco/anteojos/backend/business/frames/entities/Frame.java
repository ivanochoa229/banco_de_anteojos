package com.banco.anteojos.backend.business.frames.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "frames")
@Getter
public class Frame {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** FK plana, sin @ManyToOne: mantiene el dominio frames aislado de la entidad Donor. */
	private Long donorId;

	private String sealCode;

	@Enumerated(EnumType.STRING)
	private FrameType frameType;

	@Enumerated(EnumType.STRING)
	private FrameMaterial material;

	/** Ancho del cristal en mm. */
	private Integer lensWidthMm;

	/** Ancho del puente en mm. */
	private Integer bridgeWidthMm;

	/** Largo de la patilla en mm. */
	private Integer templeLengthMm;

	@Enumerated(EnumType.STRING)
	private FrameStatus status;

	private LocalDateTime receivedAt;

	protected Frame() {
	}

	public Frame(Long donorId, String sealCode, FrameType frameType, FrameMaterial material,
			Integer lensWidthMm, Integer bridgeWidthMm, Integer templeLengthMm) {
		this.donorId = donorId;
		// El precinto se escribe a mano sobre el marco; normalizar a mayúsculas evita que
		// "a-100" y "A-100" convivan como dos marcos distintos.
		this.sealCode = sealCode.trim().toUpperCase();
		this.frameType = frameType;
		this.material = material;
		this.lensWidthMm = lensWidthMm;
		this.bridgeWidthMm = bridgeWidthMm;
		this.templeLengthMm = templeLengthMm;
		this.status = FrameStatus.AVAILABLE;
		this.receivedAt = LocalDateTime.now();
	}

	public static String normalizeSealCode(String sealCode) {
		return sealCode.trim().toUpperCase();
	}
}
