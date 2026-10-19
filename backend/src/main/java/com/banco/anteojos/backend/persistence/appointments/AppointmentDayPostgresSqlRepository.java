package com.banco.anteojos.backend.persistence.appointments;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banco.anteojos.backend.business.appointments.entities.AppointmentDay;

import jakarta.persistence.LockModeType;

public interface AppointmentDayPostgresSqlRepository extends JpaRepository<AppointmentDay, Long> {

	List<AppointmentDay> findByDateGreaterThanEqualOrderByDateAsc(LocalDate from);

	boolean existsByDate(LocalDate date);

	boolean existsByDateAndIdNot(LocalDate date, Long id);

	/**
	 * Serializa las reservas sobre un mismo día: dos beneficiarios pidiendo a la vez el último turno
	 * leerían las mismas franjas libres. Con el día bloqueado, el segundo espera y ve la franja tomada.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT d FROM AppointmentDay d WHERE d.id = :id")
	Optional<AppointmentDay> findByIdForUpdate(@Param("id") Long id);
}
