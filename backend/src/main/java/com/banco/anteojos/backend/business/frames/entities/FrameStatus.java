package com.banco.anteojos.backend.business.frames.entities;

/**
 * Ciclo de vida del marco, desde que entra por una donación hasta que se entrega.
 * Las transiciones las maneja el dominio assignments; acá el marco solo nace AVAILABLE.
 */
public enum FrameStatus {

	/** En inventario, sin asignar. */
	AVAILABLE,
	/** Reservado para un beneficiario. */
	ASSIGNED,
	/** En la óptica, que le coloca los cristales según la receta. */
	AT_OPTICIAN,
	/** Devuelto por la óptica con los cristales puestos, listo para entregar. */
	READY,
	/** Entregado al beneficiario. */
	DELIVERED,
	/** Fuera de circulación (rotura, deterioro). */
	DISCARDED
}
