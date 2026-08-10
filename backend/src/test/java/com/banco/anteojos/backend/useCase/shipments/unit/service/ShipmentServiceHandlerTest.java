package com.banco.anteojos.backend.useCase.shipments.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banco.anteojos.backend.business.shipments.ShipmentServiceHandler;
import com.banco.anteojos.backend.business.shipments.TrackingUpdate;
import com.banco.anteojos.backend.business.shipments.TrackingWebhookHelper;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentResponseDto;
import com.banco.anteojos.backend.business.shipments.entities.Shipment;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentEvent;
import com.banco.anteojos.backend.business.shipments.exception.ShipmentNotFoundException;
import com.banco.anteojos.backend.business.shipments.exception.TrackingNumberAlreadyExistsException;
import com.banco.anteojos.backend.persistence.shipments.ShipmentEventPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.shipments.ShipmentPostgresSqlRepository;
import com.banco.anteojos.backend.thirdPartyServiceComunication.tracking.TrackingClient;
import com.banco.anteojos.backend.thirdPartyServiceComunication.tracking.TrackingRejectedException;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ShipmentServiceHandlerTest {

	@Mock
	private ShipmentPostgresSqlRepository shipmentRepository;

	@Mock
	private ShipmentEventPostgresSqlRepository eventRepository;

	@Mock
	private TrackingWebhookHelper webhookHelper;

	@Mock
	private TrackingClient trackingClient;

	@InjectMocks
	private ShipmentServiceHandler shipmentServiceHandler;

	private Shipment shipment() {
		return new Shipment("San Miguel de Tucumán", "Concepción", List.of(7L, 9L), null);
	}

	private Shipment dispatched() {
		Shipment shipment = shipment();
		shipment.dispatch("VC0012345678");
		return shipment;
	}

	private void mockSaveEcho() {
		when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	void CreateShipment_Successful() {
		mockSaveEcho();

		ShipmentResponseDto response = shipmentServiceHandler.createShipment(
				new ShipmentCreationRequestDto("San Miguel de Tucumán", "Concepción", List.of(7L, 9L),
						"frágil"));

		assertThat(response.status()).isEqualTo("PENDING");
		assertThat(response.frameIds()).containsExactly(7L, 9L);
		assertThat(response.trackingNumber()).isNull();
	}

	@Test
	void GetShipment_WhenNotFound() {
		when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> shipmentServiceHandler.getShipment(999L))
				.isInstanceOf(ShipmentNotFoundException.class);
	}

	@Test
	void Dispatch_Successful() {
		when(shipmentRepository.existsByTrackingNumber("VC0012345678")).thenReturn(false);
		when(shipmentRepository.findById(10L)).thenReturn(Optional.of(shipment()));
		mockSaveEcho();

		ShipmentResponseDto response = shipmentServiceHandler.dispatch(10L, "  VC0012345678  ");

		assertThat(response.status()).isEqualTo("REGISTERED");
		assertThat(response.trackingNumber()).isEqualTo("VC0012345678");
		assertThat(response.dispatchedAt()).isNotNull();
		verify(trackingClient).register("VC0012345678");
	}

	@Test
	void Dispatch_WhenTrackingNumberAlreadyExists() {
		when(shipmentRepository.existsByTrackingNumber("VC0012345678")).thenReturn(true);

		assertThatThrownBy(() -> shipmentServiceHandler.dispatch(10L, "VC0012345678"))
				.isInstanceOf(TrackingNumberAlreadyExistsException.class);
		verify(trackingClient, never()).register(any());
	}

	@Test
	void Dispatch_WhenTrackingRegistrationFails() {
		when(shipmentRepository.existsByTrackingNumber("VC0012345678")).thenReturn(false);
		when(shipmentRepository.findById(10L)).thenReturn(Optional.of(shipment()));
		doThrow(new TrackingRejectedException()).when(trackingClient).register("VC0012345678");

		// Si 17TRACK no lo acepta, el envío sigue pendiente: nada se persiste.
		assertThatThrownBy(() -> shipmentServiceHandler.dispatch(10L, "VC0012345678"))
				.isInstanceOf(TrackingRejectedException.class);
		verify(shipmentRepository, never()).save(any());
	}

	@Test
	void Cancel_Successful() {
		when(shipmentRepository.findById(10L)).thenReturn(Optional.of(shipment()));
		mockSaveEcho();

		ShipmentResponseDto response = shipmentServiceHandler.cancel(10L, "faltó un marco");

		assertThat(response.status()).isEqualTo("CANCELLED");
		assertThat(response.cancellationReason()).isEqualTo("faltó un marco");
	}

	@Test
	void ProcessTrackingPush_Successful() {
		when(webhookHelper.parse("body")).thenReturn(Optional.of(
				new TrackingUpdate("VC0012345678", "InTransit", "En viaje", null, null)));
		when(shipmentRepository.findByTrackingNumber("VC0012345678"))
				.thenReturn(Optional.of(dispatched()));
		mockSaveEcho();

		shipmentServiceHandler.processTrackingPush("firma", "body");

		verify(webhookHelper).verifySignature("body", "firma");
		verify(shipmentRepository).save(any(Shipment.class));
		verify(eventRepository).save(any(ShipmentEvent.class));
	}

	@Test
	void ProcessTrackingPush_WhenNumberIsUnknown() {
		when(webhookHelper.parse("body")).thenReturn(Optional.of(
				new TrackingUpdate("VC404", "InTransit", null, null, null)));
		when(shipmentRepository.findByTrackingNumber("VC404")).thenReturn(Optional.empty());

		// Silencioso a propósito: un 4xx haría que 17TRACK reintente para siempre.
		shipmentServiceHandler.processTrackingPush("firma", "body");

		verify(shipmentRepository, never()).save(any());
		verify(eventRepository, never()).save(any());
	}

	@Test
	void ProcessTrackingPush_WhenStatusIsUnmapped() {
		when(webhookHelper.parse("body")).thenReturn(Optional.of(
				new TrackingUpdate("VC0012345678", "AlgoNuevoDelCarrier", "???", null, null)));
		when(shipmentRepository.findByTrackingNumber("VC0012345678"))
				.thenReturn(Optional.of(dispatched()));

		shipmentServiceHandler.processTrackingPush("firma", "body");

		// El estado no se toca, pero el evento queda en la trazabilidad.
		verify(shipmentRepository, never()).save(any());
		verify(eventRepository).save(any(ShipmentEvent.class));
	}

	@Test
	void ProcessTrackingPush_WhenEventIsNotATrackingUpdate() {
		when(webhookHelper.parse("body")).thenReturn(Optional.empty());

		shipmentServiceHandler.processTrackingPush("firma", "body");

		verify(shipmentRepository, never()).findByTrackingNumber(any());
	}
}
