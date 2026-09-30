package com.banco.anteojos.backend.useCase.appointments.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.banco.anteojos.backend.business.appointments.entities.AppointmentDay;
import com.banco.anteojos.backend.business.appointments.exception.InvalidAppointmentDayException;

@Tag("unit")
class AppointmentDayTest {

	private static final LocalDate DATE = LocalDate.now().plusDays(10);

	// Un viernes típico: 9:00 a 10:00 en cuatro turnos de 15 minutos.
	private AppointmentDay day() {
		return new AppointmentDay(DATE, LocalTime.of(9, 0), 15, 4);
	}

	@Test
	void SlotTimes_Successful() {
		assertThat(day().slotTimes()).containsExactly(DATE.atTime(9, 0), DATE.atTime(9, 15),
				DATE.atTime(9, 30), DATE.atTime(9, 45));
		assertThat(day().endTime()).isEqualTo(LocalTime.of(10, 0));
	}

	@Test
	void SlotCountBetween_Successful() {
		assertThat(AppointmentDay.slotCountBetween(LocalTime.of(9, 0), LocalTime.of(13, 0), 15)).isEqualTo(16);
		// Lo que no llega a un turno entero al final queda sin usar.
		assertThat(AppointmentDay.slotCountBetween(LocalTime.of(9, 0), LocalTime.of(13, 0), 25)).isEqualTo(9);
	}

	@Test
	void SlotCountBetween_WhenEndIsNotAfterStart() {
		assertThatThrownBy(() -> AppointmentDay.slotCountBetween(LocalTime.of(9, 0), LocalTime.of(9, 0), 15))
				.isInstanceOf(InvalidAppointmentDayException.class)
				.hasMessageContaining("posterior");
	}

	@Test
	void Create_WhenSlotsGoPastMidnight() {
		assertThatThrownBy(() -> new AppointmentDay(DATE, LocalTime.of(23, 0), 30, 3))
				.isInstanceOf(InvalidAppointmentDayException.class)
				.hasMessageContaining("medianoche");
	}

	@Test
	void FirstAvailableSlot_SkipsBookedSlots() {
		List<LocalDateTime> booked = List.of(DATE.atTime(9, 0), DATE.atTime(9, 30));

		assertThat(day().firstAvailableSlot(booked, LocalDateTime.now())).contains(DATE.atTime(9, 15));
		assertThat(day().availableSlots(booked, LocalDateTime.now()))
				.containsExactly(DATE.atTime(9, 15), DATE.atTime(9, 45));
	}

	@Test
	void FirstAvailableSlot_WhenFull() {
		AppointmentDay day = day();

		assertThat(day.firstAvailableSlot(day.slotTimes(), LocalDateTime.now())).isEmpty();
		assertThat(day.hasPassed(LocalDateTime.now())).isFalse();
	}

	@Test
	void FirstAvailableSlot_SkipsSlotsAlreadyPassed() {
		// Mismo día del turno, 9:20: las franjas de 9:00 y 9:15 ya no se pueden ofrecer.
		LocalDateTime now = DATE.atTime(9, 20);

		assertThat(day().firstAvailableSlot(List.of(), now)).contains(DATE.atTime(9, 30));
		assertThat(day().hasPassed(DATE.atTime(9, 45))).isTrue();
	}

	@Test
	void Reconfigure_WhenNoBookings() {
		AppointmentDay day = day();

		day.reconfigure(DATE.plusDays(1), LocalTime.of(10, 0), 20, 6, List.of());

		assertThat(day.getDate()).isEqualTo(DATE.plusDays(1));
		assertThat(day.getStartTime()).isEqualTo(LocalTime.of(10, 0));
		assertThat(day.getSlotDurationMinutes()).isEqualTo(20);
		assertThat(day.getSlotCount()).isEqualTo(6);
	}

	@Test
	void Reconfigure_WhenBookedIncreasesSlotCount() {
		AppointmentDay day = day();

		day.reconfigure(DATE, LocalTime.of(9, 0), 15, 8, List.of(DATE.atTime(9, 45)));

		// Las franjas nuevas se agregan al final: la de las 9:45 sigue siendo la misma.
		assertThat(day.slotTimes()).hasSize(8).contains(DATE.atTime(9, 45));
	}

	@Test
	void Reconfigure_WhenBookedChangesStartTime() {
		AppointmentDay day = day();

		assertThatThrownBy(() -> day.reconfigure(DATE, LocalTime.of(10, 0), 15, 4, List.of(DATE.atTime(9, 0))))
				.isInstanceOf(InvalidAppointmentDayException.class)
				.hasMessageContaining("solo se puede cambiar la hora de fin");
	}

	@Test
	void Reconfigure_WhenShrinkingBelowLastBookedSlot() {
		AppointmentDay day = day();

		// El turno de las 9:30 es la tercera franja: el cupo no puede bajar de 3.
		assertThatThrownBy(() -> day.reconfigure(DATE, LocalTime.of(9, 0), 15, 2, List.of(DATE.atTime(9, 30))))
				.isInstanceOf(InvalidAppointmentDayException.class)
				.hasMessageContaining("09:30");
	}

	@Test
	void Reconfigure_WhenShrinkingAboveLastBookedSlot() {
		AppointmentDay day = day();

		day.reconfigure(DATE, LocalTime.of(9, 0), 15, 3, List.of(DATE.atTime(9, 30)));

		assertThat(day.getSlotCount()).isEqualTo(3);
	}
}
