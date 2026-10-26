package com.banco.anteojos.backend.business.assignments.entities;

import java.time.LocalDateTime;

import com.banco.anteojos.backend.business.assignments.exception.FrameReservationNotAllowedException;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * El beneficiario eligió un marco en el probador virtual y lo saca del inventario por unos días,
 * hasta que el operador se lo asigna. Como en Assignment, cada final posible es un timestamp: la
 * fila cuenta cómo terminó la reserva sin una máquina de estados aparte.
 */
@Entity
@Table(name = "frame_reservations")
@Getter
public class FrameReservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long applicantId;

	private Long frameId;

	private LocalDateTime reservedAt;

	private LocalDateTime expiresAt;

	private LocalDateTime cancelledAt;

	private LocalDateTime expiredAt;

	private LocalDateTime fulfilledAt;

	protected FrameReservation() {
	}

	public FrameReservation(Long applicantId, Long frameId, int validityDays) {
		this.applicantId = applicantId;
		this.frameId = frameId;
		this.reservedAt = LocalDateTime.now();
		this.expiresAt = reservedAt.plusDays(validityDays);
	}

	public boolean isActive() {
		return cancelledAt == null && expiredAt == null && fulfilledAt == null;
	}

	public void cancel() {
		requireActive();
		this.cancelledAt = LocalDateTime.now();
	}

	public void expire() {
		requireActive();
		this.expiredAt = LocalDateTime.now();
	}

	/** El operador le asignó al beneficiario el marco que había reservado. */
	public void fulfill() {
		requireActive();
		this.fulfilledAt = LocalDateTime.now();
	}

	private void requireActive() {
		if (!isActive()) {
			throw new FrameReservationNotAllowedException("La reserva ya no está vigente");
		}
	}
}
