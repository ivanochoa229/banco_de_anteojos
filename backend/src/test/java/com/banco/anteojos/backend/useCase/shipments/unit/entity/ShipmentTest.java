package com.banco.anteojos.backend.useCase.shipments.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.banco.anteojos.backend.business.shipments.entities.Shipment;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentStatus;
import com.banco.anteojos.backend.business.shipments.exception.InvalidShipmentTransitionException;

@Tag("unit")
class ShipmentTest {

	private Shipment shipment() {
		return new Shipment("  San Miguel de Tucumán  ", "Concepción", List.of(7L, 9L, 7L),
				"  van envueltos por separado  ");
	}

	private Shipment dispatched() {
		Shipment shipment = shipment();
		shipment.dispatch("VC0012345678");
		return shipment;
	}

	@Test
	void Create_Successful() {
		Shipment shipment = shipment();

		assertThat(shipment.getOriginBranch()).isEqualTo("San Miguel de Tucumán");
		assertThat(shipment.getDestinationBranch()).isEqualTo("Concepción");
		// El paquete no lleva el mismo marco dos veces: el conjunto deduplica.
		assertThat(shipment.getFrameIds()).containsExactly(7L, 9L);
		assertThat(shipment.getNotes()).isEqualTo("van envueltos por separado");
		assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.PENDING);
		assertThat(shipment.getTrackingNumber()).isNull();
		assertThat(shipment.getCreatedAt()).isNotNull();
	}

	@Test
	void Dispatch_Successful() {
		Shipment shipment = shipment();

		shipment.dispatch("  VC0012345678  ");

		assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.REGISTERED);
		assertThat(shipment.getTrackingNumber()).isEqualTo("VC0012345678");
		assertThat(shipment.getDispatchedAt()).isNotNull();
	}

	@Test
	void Dispatch_WhenAlreadyDispatched() {
		assertThatThrownBy(() -> dispatched().dispatch("VC999"))
				.isInstanceOf(InvalidShipmentTransitionException.class)
				.hasMessageContaining("ya fue despachado");
	}

	@Test
	void Dispatch_WhenCancelled() {
		Shipment shipment = shipment();
		shipment.cancel("se arma de nuevo");

		assertThatThrownBy(() -> shipment.dispatch("VC0012345678"))
				.isInstanceOf(InvalidShipmentTransitionException.class)
				.hasMessageContaining("cancelado");
	}

	@Test
	void Cancel_Successful() {
		Shipment shipment = shipment();

		shipment.cancel("  faltó un marco  ");

		assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);
		assertThat(shipment.getCancellationReason()).isEqualTo("faltó un marco");
	}

	@Test
	void Cancel_WhenDispatched() {
		// Despachado ya está en manos de Vía Cargo: no hay vuelta atrás desde el sistema.
		assertThatThrownBy(() -> dispatched().cancel("me arrepentí"))
				.isInstanceOf(InvalidShipmentTransitionException.class)
				.hasMessageContaining("ya fue despachado");
	}

	@Test
	void ApplyTrackingUpdate_Successful() {
		Shipment shipment = dispatched();

		shipment.applyTrackingUpdate(ShipmentStatus.IN_TRANSIT);

		assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
		assertThat(shipment.getDeliveredAt()).isNull();
	}

	@Test
	void ApplyTrackingUpdate_WhenDelivered() {
		Shipment shipment = dispatched();

		shipment.applyTrackingUpdate(ShipmentStatus.DELIVERED);

		assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
		assertThat(shipment.getDeliveredAt()).isNotNull();
	}

	@Test
	void ApplyTrackingUpdate_WhenPending() {
		assertThatThrownBy(() -> shipment().applyTrackingUpdate(ShipmentStatus.IN_TRANSIT))
				.isInstanceOf(InvalidShipmentTransitionException.class)
				.hasMessageContaining("no está despachado");
	}
}
