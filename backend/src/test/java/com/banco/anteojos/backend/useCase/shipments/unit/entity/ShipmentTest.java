package com.banco.anteojos.backend.useCase.shipments.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.banco.anteojos.backend.business.shipments.entities.Shipment;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentDirection;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentStatus;
import com.banco.anteojos.backend.business.shipments.exception.FrameFromAnotherDonorException;
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

	private Shipment inbound() {
		return Shipment.inbound(3L, "  999038143994  ", "  dice que son 4 marcos  ");
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
		assertThat(shipment.getDirection()).isEqualTo(ShipmentDirection.OUTBOUND);
		assertThat(shipment.getDonorId()).isNull();
	}

	@Test
	void CreateInbound_Successful() {
		Shipment shipment = inbound();

		assertThat(shipment.getDirection()).isEqualTo(ShipmentDirection.INBOUND);
		assertThat(shipment.getDonorId()).isEqualTo(3L);
		// Lo despachó el donante: nace con la guía y ya registrado, sin pasar por PENDING.
		assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.REGISTERED);
		assertThat(shipment.getTrackingNumber()).isEqualTo("999038143994");
		assertThat(shipment.getNotes()).isEqualTo("dice que son 4 marcos");
		assertThat(shipment.getDispatchedAt()).isNotNull();
		assertThat(shipment.getOriginBranch()).isNull();
		assertThat(shipment.getDestinationBranch()).isNull();
		assertThat(shipment.getFrameIds()).isEmpty();
	}

	@Test
	void Cancel_WhenInbound() {
		// La donación ya está en manos de Vía Cargo, igual que un saliente despachado.
		assertThatThrownBy(() -> inbound().cancel("no llegó"))
				.isInstanceOf(InvalidShipmentTransitionException.class);
	}

	@Test
	void Receive_Successful() {
		Shipment shipment = inbound();
		shipment.applyTrackingUpdate(ShipmentStatus.IN_TRANSIT);

		shipment.receive(List.of(400L, 401L), List.of(3L, 3L));

		assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.RECEIVED);
		assertThat(shipment.getFrameIds()).containsExactly(400L, 401L);
		assertThat(shipment.getReceivedAt()).isNotNull();
	}

	@Test
	void Receive_WithoutFrames() {
		Shipment shipment = inbound();

		shipment.receive(List.of(), List.of());

		assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.RECEIVED);
		assertThat(shipment.getFrameIds()).isEmpty();
	}

	@Test
	void Receive_WhenFrameIsFromAnotherDonor() {
		Shipment shipment = inbound();

		assertThatThrownBy(() -> shipment.receive(List.of(400L, 401L), List.of(3L, 8L)))
				.isInstanceOf(FrameFromAnotherDonorException.class);
		assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.REGISTERED);
	}

	@Test
	void Receive_WhenAlreadyReceived() {
		Shipment shipment = inbound();
		shipment.receive(List.of(400L), List.of(3L));

		assertThatThrownBy(() -> shipment.receive(List.of(401L), List.of(3L)))
				.isInstanceOf(InvalidShipmentTransitionException.class)
				.hasMessageContaining("ya fue recibida");
	}

	@Test
	void Receive_WhenOutbound() {
		assertThatThrownBy(() -> dispatched().receive(List.of(7L), List.of(3L)))
				.isInstanceOf(InvalidShipmentTransitionException.class)
				.hasMessageContaining("Solo se reciben");
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
	void ApplyTrackingUpdate_WhenReceived() {
		Shipment shipment = inbound();
		shipment.receive(List.of(400L), List.of(3L));

		// El "entregado" del carrier llega después de que el operador abrió el paquete.
		shipment.applyTrackingUpdate(ShipmentStatus.DELIVERED);

		assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.RECEIVED);
		assertThat(shipment.getDeliveredAt()).isNull();
	}

	@Test
	void ApplyTrackingUpdate_WhenPending() {
		assertThatThrownBy(() -> shipment().applyTrackingUpdate(ShipmentStatus.IN_TRANSIT))
				.isInstanceOf(InvalidShipmentTransitionException.class)
				.hasMessageContaining("no está despachado");
	}
}
