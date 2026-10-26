package com.banco.anteojos.backend.persistence.appointments;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banco.anteojos.backend.business.appointments.entities.Appointment;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus;

public interface AppointmentPostgresSqlRepository extends JpaRepository<Appointment, Long> {

	List<Appointment> findByApplicantIdOrderByScheduledAtDesc(Long applicantId);

	// Con excludeCancelled queda lo que todavía hay que atender: un turno cancelado ya liberó la franja.
	String UNLESS_EXCLUDED_CANCELLED = "(:excludeCancelled = false OR a.status <> "
			+ "com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus.CANCELLED)";

	@Query("SELECT a FROM Appointment a WHERE " + UNLESS_EXCLUDED_CANCELLED
			+ " ORDER BY a.scheduledAt DESC, a.id DESC")
	Page<Appointment> findAllLatestFirst(@Param("excludeCancelled") boolean excludeCancelled, Pageable pageable);

	@Query("SELECT a FROM Appointment a WHERE a.scheduledAt >= :from AND " + UNLESS_EXCLUDED_CANCELLED
			+ " ORDER BY a.scheduledAt ASC, a.id ASC")
	Page<Appointment> findFrom(@Param("from") LocalDateTime from,
			@Param("excludeCancelled") boolean excludeCancelled, Pageable pageable);

	/** La agenda de un día: [inicio del día, inicio del día siguiente). */
	@Query("SELECT a FROM Appointment a WHERE a.scheduledAt >= :from AND a.scheduledAt < :to AND "
			+ UNLESS_EXCLUDED_CANCELLED + " ORDER BY a.scheduledAt ASC, a.id ASC")
	Page<Appointment> findByDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
			@Param("excludeCancelled") boolean excludeCancelled, Pageable pageable);

	/** Turnos que ocupan franja en esos días: todos menos los cancelados, que la liberan. */
	List<Appointment> findByAppointmentDayIdInAndStatusNot(Collection<Long> appointmentDayIds,
			AppointmentStatus status);

	long countByStatusAndScheduledAtBetween(AppointmentStatus status, LocalDateTime from, LocalDateTime to);
}
