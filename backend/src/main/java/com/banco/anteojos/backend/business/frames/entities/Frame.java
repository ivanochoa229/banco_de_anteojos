package com.banco.anteojos.backend.business.frames.entities;

import java.time.LocalDateTime;

import com.banco.anteojos.backend.business.frames.exception.InvalidFrameTransitionException;

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

	/** Foto recortada del marco para el probador virtual. Null mientras nadie le saque una. */
	private String imageKey;

	private String imageContentType;

	private String imageOriginalName;

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

	/**
	 * Adjunta la foto del marco y devuelve la key de la anterior (null si no había), para que
	 * quien llama pueda borrar el objeto reemplazado en R2.
	 */
	public String attachImage(String key, String contentType, String originalName) {
		String previousKey = this.imageKey;
		this.imageKey = key;
		this.imageContentType = contentType;
		this.imageOriginalName = originalName;
		return previousKey;
	}

	public boolean hasImage() {
		return imageKey != null;
	}

	/** Reserva el marco para un beneficiario: solo se puede sacar del inventario disponible. */
	public void markAsAssigned() {
		requireStatus(FrameStatus.AVAILABLE, "El marco no está disponible en el inventario");
		this.status = FrameStatus.ASSIGNED;
	}

	public void markAsAtOptician() {
		requireStatus(FrameStatus.ASSIGNED, "El marco no está asignado a un beneficiario");
		this.status = FrameStatus.AT_OPTICIAN;
	}

	public void markAsReady() {
		requireStatus(FrameStatus.AT_OPTICIAN, "El marco no está en la óptica");
		this.status = FrameStatus.READY;
	}

	public void markAsDelivered() {
		requireStatus(FrameStatus.READY, "El marco todavía no tiene los cristales colocados");
		this.status = FrameStatus.DELIVERED;
	}

	/**
	 * Vuelve al inventario cuando se cancela la asignación. Un marco ya entregado no vuelve:
	 * está puesto en la cara de alguien.
	 *
	 * <p>Cancelar con el marco en la óptica también lo deja disponible, a propósito: la
	 * fundación puede pedirlo de vuelta, y el inventario refleja a qué marco se le puede dar
	 * otro destino, no dónde está parado físicamente en este momento.
	 */
	public void returnToInventory() {
		if (status == FrameStatus.DELIVERED || status == FrameStatus.DISCARDED) {
			throw new InvalidFrameTransitionException("El marco ya salió de circulación y no vuelve al inventario");
		}
		this.status = FrameStatus.AVAILABLE;
	}

	private void requireStatus(FrameStatus expected, String message) {
		if (status != expected) {
			throw new InvalidFrameTransitionException(message);
		}
	}
}
