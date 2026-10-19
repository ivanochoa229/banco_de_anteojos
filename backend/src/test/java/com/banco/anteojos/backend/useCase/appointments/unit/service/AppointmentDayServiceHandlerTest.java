package com.banco.anteojos.backend.useCase.appointments.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.banco.anteojos.backend.business.appointments.AppointmentDayServiceHandler;
import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentDayRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentDayResponseDto;
import com.banco.anteojos.backend.business.appointments.entities.Appointment;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentDay;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus;
import com.banco.anteojos.backend.business.appointments.exception.AppointmentDayNotFoundException;
import com.banco.anteojos.backend.business.appointments.exception.InvalidAppointmentDayException;
import com.banco.anteojos.backend.persistence.appointments.AppointmentDayPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.appointments.AppointmentPostgresSqlRepository;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AppointmentDayServiceHandlerTest {

	private static final LocalDate DATE = LocalDate.now().plusDays(10);

	@Mock
	private AppointmentDayPostgresSqlRepository appointmentDayRepository;

	@Mock
	private AppointmentPostgresSqlRepository appointmentRepository;

	@InjectMocks
	private AppointmentDayServiceHandler appointmentDayServiceHandler;

	private AppointmentDay day(Long id, LocalDate date) {
		AppointmentDay day = new AppointmentDay(date, LocalTime.of(9, 0), 15, 4);
		ReflectionTestUtils.setField(day, "id", id);
		return day;
	}

	// Turnos de 15 minutos desde las 9:00: el fin se arma para que entren exactamente slotCount.
	private AppointmentDayRequestDto request(LocalDate date, int slotCount) {
		return new AppointmentDayRequestDto(date, LocalTime.of(9, 0), LocalTime.of(9, 0).plusMinutes(15L * slotCount),
				15);
	}

	private Appointment bookedAt(Long dayId, int hour, int minute) {
		return new Appointment(1L, null, dayId, DATE.atTime(hour, minute), null);
	}

	@Test
	void CreateDay_Successful() {
		when(appointmentDayRepository.save(any(AppointmentDay.class))).thenAnswer(inv -> inv.getArgument(0));

		AppointmentDayResponseDto response = appointmentDayServiceHandler.createDay(request(DATE, 4));

		assertThat(response.date()).isEqualTo(DATE);
		assertThat(response.endTime()).isEqualTo(LocalTime.of(10, 0));
		assertThat(response.bookedCount()).isZero();
		assertThat(response.availableCount()).isEqualTo(4);
	}

	@Test
	void CreateDay_WhenRangeDoesNotDivideEvenly() {
		when(appointmentDayRepository.save(any(AppointmentDay.class))).thenAnswer(inv -> inv.getArgument(0));

		// 09:00 a 13:00 cada 25 minutos: entran 9 turnos y el último termina 12:45.
		AppointmentDayResponseDto response = appointmentDayServiceHandler.createDay(
				new AppointmentDayRequestDto(DATE, LocalTime.of(9, 0), LocalTime.of(13, 0), 25));

		assertThat(response.slotCount()).isEqualTo(9);
		assertThat(response.endTime()).isEqualTo(LocalTime.of(12, 45));
	}

	@Test
	void CreateDay_WhenEndIsBeforeStart() {
		assertThatThrownBy(() -> appointmentDayServiceHandler.createDay(
				new AppointmentDayRequestDto(DATE, LocalTime.of(13, 0), LocalTime.of(9, 0), 15)))
				.isInstanceOf(InvalidAppointmentDayException.class)
				.hasMessage("La hora de fin tiene que ser posterior a la de inicio");
		verify(appointmentDayRepository, never()).save(any(AppointmentDay.class));
	}

	@Test
	void CreateDay_WhenNoSlotFits() {
		assertThatThrownBy(() -> appointmentDayServiceHandler.createDay(
				new AppointmentDayRequestDto(DATE, LocalTime.of(9, 0), LocalTime.of(9, 10), 15)))
				.isInstanceOf(InvalidAppointmentDayException.class)
				.hasMessageContaining("no entra ni un turno");
	}

	@Test
	void CreateDay_WhenDateAlreadyExists() {
		when(appointmentDayRepository.existsByDate(DATE)).thenReturn(true);

		assertThatThrownBy(() -> appointmentDayServiceHandler.createDay(request(DATE, 4)))
				.isInstanceOf(InvalidAppointmentDayException.class)
				.hasMessage("Ya hay un día de atención cargado para esa fecha");
		verify(appointmentDayRepository, never()).save(any(AppointmentDay.class));
	}

	@Test
	void ListBookableDays_OnlyDaysWithFreeSlots() {
		AppointmentDay full = day(1L, DATE);
		AppointmentDay withRoom = day(2L, DATE.plusDays(14));
		when(appointmentDayRepository.findByDateGreaterThanEqualOrderByDateAsc(LocalDate.now()))
				.thenReturn(List.of(full, withRoom));
		when(appointmentRepository.findByAppointmentDayIdInAndStatusNot(anyCollection(),
				any(AppointmentStatus.class)))
				.thenReturn(List.of(bookedAt(1L, 9, 0), bookedAt(1L, 9, 15), bookedAt(1L, 9, 30),
						bookedAt(1L, 9, 45)));

		List<AppointmentDayResponseDto> upcoming = appointmentDayServiceHandler.listUpcomingDays();
		List<AppointmentDayResponseDto> bookable = appointmentDayServiceHandler.listBookableDays();

		assertThat(upcoming).extracting(AppointmentDayResponseDto::bookedCount).containsExactly(4, 0);
		assertThat(bookable).extracting(AppointmentDayResponseDto::id).containsExactly(2L);
		assertThat(bookable.get(0).availableCount()).isEqualTo(4);
	}

	@Test
	void UpdateDay_WhenShrinkingBelowBookings() {
		when(appointmentDayRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(day(1L, DATE)));
		when(appointmentRepository.findByAppointmentDayIdInAndStatusNot(List.of(1L), AppointmentStatus.CANCELLED))
				.thenReturn(List.of(bookedAt(1L, 9, 30)));

		assertThatThrownBy(() -> appointmentDayServiceHandler.updateDay(1L, request(DATE, 2)))
				.isInstanceOf(InvalidAppointmentDayException.class);
		verify(appointmentDayRepository, never()).save(any(AppointmentDay.class));
	}

	@Test
	void UpdateDay_WhenNotFound() {
		when(appointmentDayRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> appointmentDayServiceHandler.updateDay(99L, request(DATE, 4)))
				.isInstanceOf(AppointmentDayNotFoundException.class);
	}

	@Test
	void DeleteDay_WhenHasActiveBookings() {
		when(appointmentDayRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(day(1L, DATE)));
		when(appointmentRepository.findByAppointmentDayIdInAndStatusNot(List.of(1L), AppointmentStatus.CANCELLED))
				.thenReturn(List.of(bookedAt(1L, 9, 0)));

		assertThatThrownBy(() -> appointmentDayServiceHandler.deleteDay(1L))
				.isInstanceOf(InvalidAppointmentDayException.class)
				.hasMessageContaining("turnos activos");
		verify(appointmentDayRepository, never()).delete(any(AppointmentDay.class));
	}

	@Test
	void DeleteDay_Successful() {
		AppointmentDay day = day(1L, DATE);
		when(appointmentDayRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(day));
		when(appointmentRepository.findByAppointmentDayIdInAndStatusNot(List.of(1L), AppointmentStatus.CANCELLED))
				.thenReturn(List.of());

		appointmentDayServiceHandler.deleteDay(1L);

		verify(appointmentDayRepository).delete(day);
	}
}
