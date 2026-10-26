package com.banco.anteojos.backend.useCase.shipments.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.business.frames.dto.response.FrameResponseDto;
import com.banco.anteojos.backend.business.frames.entities.FrameMaterial;
import com.banco.anteojos.backend.business.frames.entities.FrameStatus;
import com.banco.anteojos.backend.business.frames.entities.FrameType;
import com.banco.anteojos.backend.business.shipments.ShipmentServiceHandler;
import com.banco.anteojos.backend.business.shipments.TrackingUpdate;
import com.banco.anteojos.backend.business.shipments.TrackingWebhookHelper;
import com.banco.anteojos.backend.business.shipments.dto.request.InboundShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.request.ShipmentCreationRequestDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentListItemResponseDto;
import com.banco.anteojos.backend.business.shipments.dto.response.ShipmentResponseDto;
import com.banco.anteojos.backend.business.shipments.entities.Shipment;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentDirection;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentEvent;
import com.banco.anteojos.backend.business.shipments.entities.ShipmentStatus;
import com.banco.anteojos.backend.business.shipments.exception.FrameFromAnotherDonorException;
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

	private static final Pageable PAGE = PageRequest.of(0, 20);

	private Shipment shipment() {
		return new Shipment("San Miguel de Tucumán", "Concepción", List.of(7L, 9L), null);
	}

	private Shipment dispatched() {
		Shipment shipment = shipment();
		shipment.dispatch("VC0012345678");
		return shipment;
	}

	private FrameResponseDto frame(Long id, Long donorId) {
		return new FrameResponseDto(id, donorId, "A-" + id, FrameType.FULL_RIM, FrameMaterial.ACETATE, 52, 18,
				140, FrameStatus.AVAILABLE, null, LocalDateTime.now());
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
		assertThat(response.direction()).isEqualTo("OUTBOUND");
		assertThat(response.frameIds()).containsExactly(7L, 9L);
		assertThat(response.trackingNumber()).isNull();
	}

	@Test
	void CreateInboundShipment_Successful() {
		when(shipmentRepository.existsByTrackingNumber("999038143994")).thenReturn(false);
		mockSaveEcho();

		ShipmentResponseDto response = shipmentServiceHandler.createInboundShipment(3L,
				new InboundShipmentCreationRequestDto("  999038143994  ", null));

		assertThat(response.direction()).isEqualTo("INBOUND");
		assertThat(response.donorId()).isEqualTo(3L);
		assertThat(response.status()).isEqualTo("REGISTERED");
		assertThat(response.trackingNumber()).isEqualTo("999038143994");
		verify(trackingClient).register("999038143994");
	}

	@Test
	void CreateInboundShipment_WhenTrackingNumberAlreadyExists() {
		when(shipmentRepository.existsByTrackingNumber("999038143994")).thenReturn(true);

		assertThatThrownBy(() -> shipmentServiceHandler.createInboundShipment(3L,
				new InboundShipmentCreationRequestDto("999038143994", null)))
				.isInstanceOf(TrackingNumberAlreadyExistsException.class);
		verify(trackingClient, never()).register(any());
	}

	@Test
	void CreateInboundShipment_WhenTrackingRegistrationFails() {
		when(shipmentRepository.existsByTrackingNumber("999038143994")).thenReturn(false);
		doThrow(new TrackingRejectedException()).when(trackingClient).register("999038143994");

		assertThatThrownBy(() -> shipmentServiceHandler.createInboundShipment(3L,
				new InboundShipmentCreationRequestDto("999038143994", null)))
				.isInstanceOf(TrackingRejectedException.class);
		verify(shipmentRepository, never()).save(any());
	}

	@Test
	void Receive_Successful() {
		when(shipmentRepository.findById(20L))
				.thenReturn(Optional.of(Shipment.inbound(3L, "999038143994", null)));
		mockSaveEcho();

		ShipmentResponseDto response = shipmentServiceHandler.receive(20L,
				List.of(frame(400L, 3L), frame(401L, 3L)));

		assertThat(response.status()).isEqualTo("RECEIVED");
		assertThat(response.frameIds()).containsExactly(400L, 401L);
		assertThat(response.receivedAt()).isNotNull();
	}

	@Test
	void Receive_WhenFrameIsFromAnotherDonor() {
		when(shipmentRepository.findById(20L))
				.thenReturn(Optional.of(Shipment.inbound(3L, "999038143994", null)));

		assertThatThrownBy(() -> shipmentServiceHandler.receive(20L, List.of(frame(400L, 3L), frame(401L, 8L))))
				.isInstanceOf(FrameFromAnotherDonorException.class);
		verify(shipmentRepository, never()).save(any());
	}

	@Test
	void ListShipments_FiltersByDirection() {
		when(shipmentRepository.findByDirectionOrderByCreatedAtDescIdDesc(ShipmentDirection.INBOUND, PAGE))
				.thenReturn(new PageImpl<>(List.of(Shipment.inbound(3L, "999038143994", null)), PAGE, 1));

		PageResponseDto<ShipmentResponseDto> response = shipmentServiceHandler.listShipments(ShipmentDirection.INBOUND,
				false, PAGE);

		assertThat(response.content()).singleElement()
				.satisfies(shipment -> assertThat(shipment.direction()).isEqualTo("INBOUND"));
	}

	@Test
	void ListShipments_WhenOnlyLiveOutbound() {
		when(shipmentRepository.findByDirectionAndStatusNotInOrderByCreatedAtDescIdDesc(any(), any(), any()))
				.thenReturn(new PageImpl<>(List.of(), PAGE, 0));

		shipmentServiceHandler.listShipments(ShipmentDirection.OUTBOUND, true, PAGE);

		verify(shipmentRepository).findByDirectionAndStatusNotInOrderByCreatedAtDescIdDesc(ShipmentDirection.OUTBOUND,
				Set.of(ShipmentStatus.CANCELLED, ShipmentStatus.DELIVERED, ShipmentStatus.EXPIRED), PAGE);
	}

	// Una donación que el correo dio por entregada sigue en curso hasta que se abre en la fundación.
	@Test
	void ListShipments_WhenOnlyLiveInbound() {
		when(shipmentRepository.findByDirectionAndStatusNotInOrderByCreatedAtDescIdDesc(any(), any(), any()))
				.thenReturn(new PageImpl<>(List.of(), PAGE, 0));

		shipmentServiceHandler.listShipments(ShipmentDirection.INBOUND, true, PAGE);

		verify(shipmentRepository).findByDirectionAndStatusNotInOrderByCreatedAtDescIdDesc(ShipmentDirection.INBOUND,
				Set.of(ShipmentStatus.RECEIVED), PAGE);
	}

	@Test
	void BuildListItems_AttachesDonorNameOnlyToDonations() {
		when(shipmentRepository.findByDirectionOrderByCreatedAtDescIdDesc(ShipmentDirection.INBOUND, PAGE))
				.thenReturn(new PageImpl<>(List.of(Shipment.inbound(3L, "999038143994", null),
						new Shipment("Sede Centro", "Concepción", List.of(1L), null)), PAGE, 2));
		PageResponseDto<ShipmentResponseDto> shipments = shipmentServiceHandler.listShipments(ShipmentDirection.INBOUND,
				false, PAGE);

		PageResponseDto<ShipmentListItemResponseDto> response = shipmentServiceHandler.buildListItems(shipments,
				Map.of(3L, "Óptica Norte"));

		assertThat(response.content()).extracting(ShipmentListItemResponseDto::donorName)
				.containsExactly("Óptica Norte", null);
	}

	@Test
	void ListShipmentsByDonor_Successful() {
		when(shipmentRepository.findByDonorIdOrderByCreatedAtDescIdDesc(3L, PAGE))
				.thenReturn(new PageImpl<>(List.of(Shipment.inbound(3L, "999038143994", null)), PAGE, 1));

		assertThat(shipmentServiceHandler.listShipmentsByDonor(3L, PAGE).content()).hasSize(1);
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

	@Test
	void CountDelivered_OnlyCountsOutbound() {
		LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(2026, 2, 1, 0, 0);
		when(shipmentRepository.countByDirectionAndStatusAndDeliveredAtBetween(ShipmentDirection.OUTBOUND,
				ShipmentStatus.DELIVERED, from, to)).thenReturn(2L);

		assertThat(shipmentServiceHandler.countDelivered(from, to)).isEqualTo(2L);
	}
}
