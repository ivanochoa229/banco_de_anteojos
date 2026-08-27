package com.banco.anteojos.backend.business.appointments;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.banco.anteojos.backend.business.appointments.dto.request.AppointmentCreationRequestDto;
import com.banco.anteojos.backend.business.appointments.dto.response.AppointmentResponseDto;
import com.banco.anteojos.backend.business.appointments.entities.Appointment;
import com.banco.anteojos.backend.business.appointments.entities.AppointmentStatus;
import com.banco.anteojos.backend.business.appointments.exception.AppointmentNotFoundException;
import com.banco.anteojos.backend.persistence.appointments.AppointmentPostgresSqlRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentServiceHandler implements AppointmentService {

	private final AppointmentPostgresSqlRepository appointmentRepository;

	@Override
	public AppointmentResponseDto createAppointment(Long applicantId, AppointmentCreationRequestDto request) {
		Appointment appointment = appointmentRepository.save(new Appointment(applicantId,
				request.assignmentId(), request.scheduledAt(), request.notes()));
		return toResponse(appointment);
	}

	@Override
	public AppointmentResponseDto getAppointment(Long appointmentId) {
		return toResponse(findAppointment(appointmentId));
	}

	@Override
	public List<AppointmentResponseDto> listAppointments(LocalDate date) {
		List<Appointment> appointments = date == null
				? appointmentRepository.findAllByOrderByScheduledAtDesc()
				: appointmentRepository.findByDay(date.atStartOfDay(), date.plusDays(1).atStartOfDay());
		return appointments.stream().map(this::toResponse).toList();
	}

	@Override
	public List<AppointmentResponseDto> listAppointmentsByApplicant(Long applicantId) {
		return appointmentRepository.findByApplicantIdOrderByScheduledAtDesc(applicantId).stream()
				.map(this::toResponse).toList();
	}

	@Override
	public AppointmentResponseDto reschedule(Long appointmentId, LocalDateTime newScheduledAt) {
		Appointment appointment = findAppointment(appointmentId);
		appointment.reschedule(newScheduledAt);
		return toResponse(appointmentRepository.save(appointment));
	}

	@Override
	public AppointmentResponseDto cancel(Long appointmentId, String reason) {
		Appointment appointment = findAppointment(appointmentId);
		appointment.cancel(reason);
		return toResponse(appointmentRepository.save(appointment));
	}

	@Override
	public AppointmentResponseDto registerAttendance(Long appointmentId, boolean attended) {
		Appointment appointment = findAppointment(appointmentId);
		appointment.registerAttendance(attended);
		return toResponse(appointmentRepository.save(appointment));
	}

	@Override
	public long countByStatus(AppointmentStatus status, LocalDateTime from, LocalDateTime to) {
		return appointmentRepository.countByStatusAndScheduledAtBetween(status, from, to);
	}

	private Appointment findAppointment(Long appointmentId) {
		return appointmentRepository.findById(appointmentId).orElseThrow(AppointmentNotFoundException::new);
	}

	private AppointmentResponseDto toResponse(Appointment appointment) {
		return new AppointmentResponseDto(appointment.getId(), appointment.getApplicantId(),
				appointment.getAssignmentId(), appointment.getScheduledAt(),
				appointment.getStatus().name(), appointment.getNotes(),
				appointment.getCancellationReason(), appointment.getCreatedAt());
	}
}
