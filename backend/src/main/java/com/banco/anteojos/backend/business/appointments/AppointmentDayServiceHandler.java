package com.banco.anteojos.backend.business.appointments;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentDayRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentDayResponseDto;
import com.banco.anteojos.backend.business.appointments.entities.Appointment;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentDay;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus;
import com.banco.anteojos.backend.business.appointments.exception.AppointmentDayNotFoundException;
import com.banco.anteojos.backend.business.appointments.exception.InvalidAppointmentDayException;
import com.banco.anteojos.backend.persistence.appointments.AppointmentDayPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.appointments.AppointmentPostgresSqlRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentDayServiceHandler implements AppointmentDayService {

	private final AppointmentDayPostgresSqlRepository appointmentDayRepository;
	private final AppointmentPostgresSqlRepository appointmentRepository;

	@Override
	public List<AppointmentDayResponseDto> listUpcomingDays() {
		return toResponses(appointmentDayRepository.findByDateGreaterThanEqualOrderByDateAsc(LocalDate.now()));
	}

	@Override
	public List<AppointmentDayResponseDto> listBookableDays() {
		return listUpcomingDays().stream().filter(day -> day.availableCount() > 0).toList();
	}

	@Override
	public AppointmentDayResponseDto createDay(AppointmentDayRequestDto request) {
		if (appointmentDayRepository.existsByDate(request.date())) {
			throw new InvalidAppointmentDayException("Ya hay un día de atención cargado para esa fecha");
		}
		AppointmentDay day = appointmentDayRepository.save(new AppointmentDay(request.date(), request.startTime(),
				request.slotDurationMinutes(), slotCount(request)));
		return toResponse(day, List.of(), LocalDateTime.now());
	}

	@Override
	public AppointmentDayResponseDto updateDay(Long appointmentDayId, AppointmentDayRequestDto request) {
		AppointmentDay day = lockDay(appointmentDayId);
		if (appointmentDayRepository.existsByDateAndIdNot(request.date(), appointmentDayId)) {
			throw new InvalidAppointmentDayException("Ya hay un día de atención cargado para esa fecha");
		}
		List<LocalDateTime> bookedSlots = bookedSlots(List.of(appointmentDayId))
				.getOrDefault(appointmentDayId, List.of());
		day.reconfigure(request.date(), request.startTime(), request.slotDurationMinutes(), slotCount(request),
				bookedSlots);
		return toResponse(appointmentDayRepository.save(day), bookedSlots, LocalDateTime.now());
	}

	@Override
	public void deleteDay(Long appointmentDayId) {
		AppointmentDay day = lockDay(appointmentDayId);
		if (bookedSlots(List.of(appointmentDayId)).containsKey(appointmentDayId)) {
			throw new InvalidAppointmentDayException(
					"El día tiene turnos activos: cancelalos o reprogramalos antes de borrarlo");
		}
		appointmentDayRepository.delete(day);
	}

	private int slotCount(AppointmentDayRequestDto request) {
		return AppointmentDay.slotCountBetween(request.startTime(), request.endTime(),
				request.slotDurationMinutes());
	}

	private AppointmentDay lockDay(Long appointmentDayId) {
		return appointmentDayRepository.findByIdForUpdate(appointmentDayId)
				.orElseThrow(AppointmentDayNotFoundException::new);
	}

	// Una sola consulta para todos los días del listado, agrupada en Java por día.
	private Map<Long, List<LocalDateTime>> bookedSlots(Collection<Long> appointmentDayIds) {
		return appointmentRepository
				.findByAppointmentDayIdInAndStatusNot(appointmentDayIds, AppointmentStatus.CANCELLED).stream()
				.collect(Collectors.groupingBy(Appointment::getAppointmentDayId,
						Collectors.mapping(Appointment::getScheduledAt, Collectors.toList())));
	}

	private List<AppointmentDayResponseDto> toResponses(List<AppointmentDay> days) {
		if (days.isEmpty()) {
			return List.of();
		}
		Map<Long, List<LocalDateTime>> bookedByDay = bookedSlots(days.stream().map(AppointmentDay::getId).toList());
		LocalDateTime now = LocalDateTime.now();
		return days.stream()
				.map(day -> toResponse(day, bookedByDay.getOrDefault(day.getId(), List.of()), now))
				.toList();
	}

	private AppointmentDayResponseDto toResponse(AppointmentDay day, List<LocalDateTime> bookedSlots,
			LocalDateTime now) {
		return new AppointmentDayResponseDto(day.getId(), day.getDate(), day.getStartTime(), day.endTime(),
				day.getSlotDurationMinutes(), day.getSlotCount(), bookedSlots.size(),
				day.availableSlots(bookedSlots, now).size());
	}
}
