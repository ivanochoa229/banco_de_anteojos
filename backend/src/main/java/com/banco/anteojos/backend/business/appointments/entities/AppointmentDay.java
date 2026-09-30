package com.banco.anteojos.backend.business.appointments.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import com.banco.anteojos.backend.business.appointments.exception.InvalidAppointmentDayException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * Día en que la sede atiende (RF-20). El rango se parte en {@code slotCount} franjas consecutivas de
 * {@code slotDurationMinutes}: cada turno ocupa una franja y su {@code scheduledAt} es el inicio de
 * esa franja. Qué franjas están tomadas lo sabe el repositorio de turnos; acá entra como parámetro.
 */
@Entity
@Table(name = "appointment_days")
@Getter
public class AppointmentDay {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "attention_date")
	private LocalDate date;

	private LocalTime startTime;

	private int slotDurationMinutes;

	private int slotCount;

	private LocalDateTime createdAt;

	protected AppointmentDay() {
	}

	public AppointmentDay(LocalDate date, LocalTime startTime, int slotDurationMinutes, int slotCount) {
		requireWithinTheDay(date, startTime, slotDurationMinutes, slotCount);
		this.date = date;
		this.startTime = startTime;
		this.slotDurationMinutes = slotDurationMinutes;
		this.slotCount = slotCount;
		this.createdAt = LocalDateTime.now();
	}

	/**
	 * Con turnos ya dados solo se puede tocar el cupo, y nunca por debajo de la última franja
	 * tomada: mover la fecha, el inicio o la duración le cambiaría la hora a gente ya citada.
	 */
	public void reconfigure(LocalDate newDate, LocalTime newStartTime, int newSlotDurationMinutes,
			int newSlotCount, Collection<LocalDateTime> bookedSlots) {
		requireWithinTheDay(newDate, newStartTime, newSlotDurationMinutes, newSlotCount);
		if (!bookedSlots.isEmpty()) {
			boolean scheduleChanged = !newDate.equals(date) || !newStartTime.equals(startTime)
					|| newSlotDurationMinutes != slotDurationMinutes;
			if (scheduleChanged) {
				throw new InvalidAppointmentDayException(
						"El día ya tiene turnos dados: solo se puede cambiar la cantidad de turnos");
			}
			int lastBookedSlot = slotTimes().indexOf(bookedSlots.stream().max(LocalDateTime::compareTo).get());
			if (newSlotCount <= lastBookedSlot) {
				throw new InvalidAppointmentDayException(
						"No se puede bajar el cupo a %d: el turno de las %s ya está dado"
								.formatted(newSlotCount, slotTimes().get(lastBookedSlot).toLocalTime()));
			}
		}
		this.date = newDate;
		this.startTime = newStartTime;
		this.slotDurationMinutes = newSlotDurationMinutes;
		this.slotCount = newSlotCount;
	}

	public List<LocalDateTime> slotTimes() {
		LocalDateTime first = date.atTime(startTime);
		return IntStream.range(0, slotCount)
				.mapToObj(i -> first.plusMinutes((long) i * slotDurationMinutes))
				.toList();
	}

	public LocalTime endTime() {
		return startTime.plusMinutes((long) slotCount * slotDurationMinutes);
	}

	/** Franjas libres que todavía no pasaron: en el mismo día no se ofrece una hora ya vencida. */
	public List<LocalDateTime> availableSlots(Collection<LocalDateTime> bookedSlots, LocalDateTime now) {
		return slotTimes().stream()
				.filter(slot -> slot.isAfter(now) && !bookedSlots.contains(slot))
				.toList();
	}

	/** Ya no queda ninguna franja por delante, tomada o no. */
	public boolean hasPassed(LocalDateTime now) {
		List<LocalDateTime> slots = slotTimes();
		return !slots.get(slots.size() - 1).isAfter(now);
	}

	public Optional<LocalDateTime> firstAvailableSlot(Collection<LocalDateTime> bookedSlots, LocalDateTime now) {
		return availableSlots(bookedSlots, now).stream().findFirst();
	}

	// El último turno tiene que terminar antes de medianoche: si no, la franja caería en otra fecha.
	private static void requireWithinTheDay(LocalDate date, LocalTime startTime, int slotDurationMinutes,
			int slotCount) {
		long totalMinutes = (long) slotDurationMinutes * slotCount;
		if (startTime.toSecondOfDay() / 60 + totalMinutes > 24 * 60) {
			throw new InvalidAppointmentDayException("Los turnos del día no pueden pasar de la medianoche");
		}
	}
}
