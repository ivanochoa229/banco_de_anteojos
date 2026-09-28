package com.banco.anteojos.backend.useCase.shipments.unit.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.banco.anteojos.backend.business.shipments.entities.ShipmentStatus;

@Tag("unit")
class ShipmentStatusTest {

	@ParameterizedTest
	@CsvSource({
			"InfoReceived, INFO_RECEIVED",
			"InTransit, IN_TRANSIT",
			"AvailableForPickup, AVAILABLE_FOR_PICKUP",
			"OutForDelivery, OUT_FOR_DELIVERY",
			"Delivered, DELIVERED",
			"DeliveryFailure, DELIVERY_FAILURE",
			"Exception, EXCEPTION",
			"Expired, EXPIRED",
			"NotFound, NOT_FOUND"
	})
	void FromTracking_Successful(String rawStatus, ShipmentStatus expected) {
		assertThat(ShipmentStatus.fromTracking(rawStatus)).contains(expected);
	}

	@Test
	void FromTracking_WhenCarrierSendsAnUnknownStatus() {
		assertThat(ShipmentStatus.fromTracking("SomethingNew")).isEmpty();
	}

	@Test
	void FromTracking_WhenRawStatusIsEmpty() {
		// El helper de parseo manda "" cuando 17TRACK no informa latest_status.
		assertThat(ShipmentStatus.fromTracking("")).isEmpty();
	}
}
