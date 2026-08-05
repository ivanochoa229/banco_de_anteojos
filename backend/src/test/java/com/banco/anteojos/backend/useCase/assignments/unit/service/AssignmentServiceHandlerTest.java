package com.banco.anteojos.backend.useCase.assignments.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.banco.anteojos.backend.business.assignments.AssignmentServiceHandler;
import com.banco.anteojos.backend.business.assignments.dto.request.AssignmentCreationRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentCreationResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentResponseDto;
import com.banco.anteojos.backend.business.assignments.entities.Assignment;
import com.banco.anteojos.backend.business.assignments.exception.AssignmentNotFoundException;
import com.banco.anteojos.backend.persistence.assignments.AssignmentPostgresSqlRepository;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AssignmentServiceHandlerTest {

	@Mock
	private AssignmentPostgresSqlRepository assignmentRepository;

	@InjectMocks
	private AssignmentServiceHandler assignmentServiceHandler;

	private Assignment assignment() {
		return new Assignment(1L, 7L, 3L, null);
	}

	private void mockFind(Assignment assignment) {
		when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
		when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	void CreateAssignment_Successful() {
		when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));

		AssignmentCreationResponseDto response = assignmentServiceHandler.createAssignment(1L,
				new AssignmentCreationRequestDto(7L, 3L, "retira el jueves"), null);

		assertThat(response.assignment().applicantId()).isEqualTo(1L);
		assertThat(response.assignment().frameId()).isEqualTo(7L);
		assertThat(response.assignment().prescriptionId()).isEqualTo(3L);
		assertThat(response.assignment().notes()).isEqualTo("retira el jueves");
		assertThat(response.assignment().assignedAt()).isNotNull();
		assertThat(response.eligibilityWarning()).isNull();
	}

	@Test
	void CreateAssignment_CarriesEligibilityWarning() {
		when(assignmentRepository.save(any(Assignment.class))).thenAnswer(inv -> inv.getArgument(0));

		// La advertencia no frena la asignación: solo viaja en la respuesta para el operador.
		AssignmentCreationResponseDto response = assignmentServiceHandler.createAssignment(1L,
				new AssignmentCreationRequestDto(7L, 3L, null), "Falta la negativa de ANSES");

		assertThat(response.eligibilityWarning()).isEqualTo("Falta la negativa de ANSES");
	}

	@Test
	void GetAssignment_WhenNotFound() {
		when(assignmentRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> assignmentServiceHandler.getAssignment(999L))
				.isInstanceOf(AssignmentNotFoundException.class);
	}

	@Test
	void SendToOptician_Successful() {
		mockFind(assignment());

		AssignmentResponseDto response = assignmentServiceHandler.sendToOptician(10L);

		assertThat(response.sentToOpticianAt()).isNotNull();
		assertThat(response.returnedAt()).isNull();
	}

	@Test
	void ReturnFromOptician_Successful() {
		Assignment assignment = assignment();
		assignment.sendToOptician();
		mockFind(assignment);

		assertThat(assignmentServiceHandler.returnFromOptician(10L).returnedAt()).isNotNull();
	}

	@Test
	void Deliver_Successful() {
		Assignment assignment = assignment();
		assignment.sendToOptician();
		assignment.returnFromOptician();
		mockFind(assignment);

		assertThat(assignmentServiceHandler.deliver(10L).deliveredAt()).isNotNull();
	}

	@Test
	void Cancel_Successful() {
		mockFind(assignment());

		AssignmentResponseDto response = assignmentServiceHandler.cancel(10L, "no se presentó");

		assertThat(response.cancelledAt()).isNotNull();
		assertThat(response.cancellationReason()).isEqualTo("no se presentó");
		verify(assignmentRepository).save(any(Assignment.class));
	}

	@Test
	void ListAssignments_WhenOnlyLive() {
		when(assignmentRepository.findLive()).thenReturn(List.of(assignment()));

		assertThat(assignmentServiceHandler.listAssignments(true)).hasSize(1);
		verify(assignmentRepository).findLive();
	}

	@Test
	void ListAssignments_WhenAll() {
		when(assignmentRepository.findAllByOrderByAssignedAtDesc()).thenReturn(List.of(assignment()));

		assertThat(assignmentServiceHandler.listAssignments(false)).hasSize(1);
	}

	@Test
	void ListAssignmentsByApplicant_Successful() {
		when(assignmentRepository.findByApplicantIdOrderByAssignedAtDesc(1L))
				.thenReturn(List.of(assignment()));

		assertThat(assignmentServiceHandler.listAssignmentsByApplicant(1L)).hasSize(1);
	}
}
