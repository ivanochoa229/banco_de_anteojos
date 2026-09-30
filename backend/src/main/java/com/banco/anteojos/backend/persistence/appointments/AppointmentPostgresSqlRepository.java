package com.banco.anteojos.backend.persistence.appointments;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.banco.anteojos.backend.business.appointments.entities.Appointment;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus;

public interface AppointmentPostgresSqlRepository extends JpaRepository<Appointment, Long> {

	List<Appointment> findByApplicantIdOrderByScheduledAtDesc(Long applicantId);

	List<Appointment> findAllByOrderByScheduledAtDesc();

	/** La agenda de un día: [inicio del día, inicio del día siguiente). */
	@Query("SELECT a FROM Appointment a WHERE a.scheduledAt >= :from AND a.scheduledAt < :to "
			+ "ORDER BY a.scheduledAt ASC")
	List<Appointment> findByDay(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/** Turnos que ocupan franja en esos días: todos menos los cancelados, que la liberan. */
	List<Appointment> findByAppointmentDayIdInAndStatusNot(Collection<Long> appointmentDayIds,
			AppointmentStatus status);

	long countByStatusAndScheduledAtBetween(AppointmentStatus status, LocalDateTime from, LocalDateTime to);
}
