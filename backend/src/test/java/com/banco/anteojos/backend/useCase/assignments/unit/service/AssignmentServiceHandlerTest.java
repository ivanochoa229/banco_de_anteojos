package com.banco.anteojos.backend.useCase.assignments.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.banco.anteojos.backend.business.assignments.AssignmentServiceHandler;
import com.banco.anteojos.backend.business.assignments.dto.request.AssignmentCreationRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.request.DeliveryPhotoUploadRequestDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentCreationResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentDeliveryResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentListItemResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.AssignmentResponseDto;
import com.banco.anteojos.backend.business.assignments.dto.response.MonthlyDeliveryResponseDto;
import com.banco.anteojos.backend.business.assignments.entities.Assignment;
import com.banco.anteojos.backend.business.assignments.exception.AssignmentNotFoundException;
import com.banco.anteojos.backend.business.assignments.exception.DeliveryPhotoNotFoundException;
import com.banco.anteojos.backend.business.assignments.exception.InvalidAssignmentTransitionException;
import com.banco.anteojos.backend.business.assignments.exception.InvalidDeliveryPhotoException;
import com.banco.anteojos.backend.business.common.dto.response.PageResponseDto;
import com.banco.anteojos.backend.persistence.assignments.AssignmentPostgresSqlRepository;
import com.banco.anteojos.backend.persistence.assignments.MonthlyDeliveryProjection;
import com.banco.anteojos.backend.thirdPartyServiceComunication.storage.R2StorageClient;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class AssignmentServiceHandlerTest {

	@Mock
	private AssignmentPostgresSqlRepository assignmentRepository;

	@Mock
	private R2StorageClient r2StorageClient;

	@InjectMocks
	private AssignmentServiceHandler assignmentServiceHandler;

	private static final Pageable PAGE = PageRequest.of(0, 20);

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

		AssignmentDeliveryResponseDto response = assignmentServiceHandler.deliver(10L, null, false);

		assertThat(response.assignment().deliveredAt()).isNotNull();
		assertThat(response.assignment().deliveryPhotoOriginalName()).isNull();
		assertThat(response.photoForDonor()).isNull();
		verify(r2StorageClient, never()).upload(any(), any(), any());
	}

	private Assignment returned() {
		Assignment assignment = assignment();
		assignment.sendToOptician();
		assignment.returnFromOptician();
		return assignment;
	}

	private DeliveryPhotoUploadRequestDto photo(String contentType) {
		return new DeliveryPhotoUploadRequestDto(new byte[] { 1, 2, 3 }, contentType, "entrega.jpg");
	}

	@Test
	void Deliver_WithPhotoAndConsent() {
		mockFind(returned());

		AssignmentDeliveryResponseDto response = assignmentServiceHandler.deliver(10L,
				photo("image/jpeg; charset=binary"), true);

		verify(r2StorageClient).upload(startsWith("deliveries/10/"), eq(new byte[] { 1, 2, 3 }), eq("image/jpeg"));
		assertThat(response.assignment().deliveryPhotoOriginalName()).isEqualTo("entrega.jpg");
		assertThat(response.assignment().deliveryPhotoConsent()).isTrue();
		// Con permiso, la foto viaja al donante.
		assertThat(response.photoForDonor()).isNotNull();
		assertThat(response.photoForDonor().contentType()).isEqualTo("image/jpeg");
		assertThat(response.photoForDonor().fileName()).isEqualTo("entrega.jpg");
	}

	@Test
	void Deliver_WithPhotoWithoutConsent() {
		mockFind(returned());

		AssignmentDeliveryResponseDto response = assignmentServiceHandler.deliver(10L, photo("image/png"), false);

		// Sin permiso, la foto queda en el registro pero no se le manda al donante.
		verify(r2StorageClient).upload(startsWith("deliveries/10/"), any(), eq("image/png"));
		assertThat(response.assignment().deliveryPhotoOriginalName()).isEqualTo("entrega.jpg");
		assertThat(response.assignment().deliveryPhotoConsent()).isFalse();
		assertThat(response.photoForDonor()).isNull();
	}

	@Test
	void Deliver_WhenPhotoIsNotAnImage() {
		when(assignmentRepository.findById(10L)).thenReturn(Optional.of(returned()));

		assertThatThrownBy(() -> assignmentServiceHandler.deliver(10L, photo("application/pdf"), true))
				.isInstanceOf(InvalidDeliveryPhotoException.class);
		verify(r2StorageClient, never()).upload(any(), any(), any());
		verify(assignmentRepository, never()).save(any());
	}

	@Test
	void Deliver_WhenNotReturned_DoesNotUploadPhoto() {
		when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment()));

		// Una entrega fuera de orden no puede dejar una foto huérfana en R2.
		assertThatThrownBy(() -> assignmentServiceHandler.deliver(10L, photo("image/jpeg"), true))
				.isInstanceOf(InvalidAssignmentTransitionException.class);
		verify(r2StorageClient, never()).upload(any(), any(), any());
	}

	@Test
	void GetDeliveryPhoto_WhenThereIsNone() {
		Assignment assignment = returned();
		assignment.deliver();
		when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));

		assertThatThrownBy(() -> assignmentServiceHandler.getDeliveryPhoto(10L))
				.isInstanceOf(DeliveryPhotoNotFoundException.class);
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
		when(assignmentRepository.findLive(PAGE)).thenReturn(new PageImpl<>(List.of(assignment()), PAGE, 1));

		assertThat(assignmentServiceHandler.listAssignments(true, PAGE).content()).hasSize(1);
		verify(assignmentRepository).findLive(PAGE);
	}

	@Test
	void ListAssignments_WhenAll() {
		when(assignmentRepository.findAllByOrderByAssignedAtDescIdDesc(PAGE))
				.thenReturn(new PageImpl<>(List.of(assignment()), PAGE, 1));

		assertThat(assignmentServiceHandler.listAssignments(false, PAGE).content()).hasSize(1);
	}

	@Test
	void BuildListItems_AttachesApplicantNameAndSealCode() {
		when(assignmentRepository.findLive(PAGE)).thenReturn(new PageImpl<>(List.of(assignment()), PAGE, 1));
		PageResponseDto<AssignmentResponseDto> assignments = assignmentServiceHandler.listAssignments(true, PAGE);

		PageResponseDto<AssignmentListItemResponseDto> response = assignmentServiceHandler.buildListItems(
				assignments, Map.of(1L, "Pérez, Juan"), Map.of(7L, "PR-0007"));

		assertThat(response.content()).singleElement().satisfies(item -> {
			assertThat(item.applicantName()).isEqualTo("Pérez, Juan");
			assertThat(item.frameSealCode()).isEqualTo("PR-0007");
		});
	}

	@Test
	void BuildListItems_WhenReferenceNoLongerExists() {
		when(assignmentRepository.findLive(PAGE)).thenReturn(new PageImpl<>(List.of(assignment()), PAGE, 1));
		PageResponseDto<AssignmentResponseDto> assignments = assignmentServiceHandler.listAssignments(true, PAGE);

		PageResponseDto<AssignmentListItemResponseDto> response = assignmentServiceHandler.buildListItems(
				assignments, Map.of(), Map.of());

		assertThat(response.content()).singleElement().satisfies(item -> {
			assertThat(item.applicantName()).isNull();
			assertThat(item.frameSealCode()).isNull();
		});
	}

	@Test
	void ListAssignmentsByApplicant_Successful() {
		when(assignmentRepository.findByApplicantIdOrderByAssignedAtDesc(1L))
				.thenReturn(List.of(assignment()));

		assertThat(assignmentServiceHandler.listAssignmentsByApplicant(1L)).hasSize(1);
	}

	@Test
	void CountDelivered_DelegatesToRepository() {
		LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(2026, 2, 1, 0, 0);
		when(assignmentRepository.countDelivered(from, to)).thenReturn(5L);

		assertThat(assignmentServiceHandler.countDelivered(from, to)).isEqualTo(5L);
	}

	@Test
	void CountDistinctApplicantsServed_DelegatesToRepository() {
		LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(2026, 2, 1, 0, 0);
		when(assignmentRepository.countDistinctApplicantsServed(from, to)).thenReturn(4L);

		assertThat(assignmentServiceHandler.countDistinctApplicantsServed(from, to)).isEqualTo(4L);
	}

	@Test
	void DeliveriesByMonth_MapsProjectionToDto() {
		LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
		LocalDateTime to = LocalDateTime.of(2026, 3, 1, 0, 0);
		MonthlyDeliveryProjection row = mock(MonthlyDeliveryProjection.class);
		when(row.getMonth()).thenReturn(LocalDateTime.of(2026, 1, 1, 0, 0));
		when(row.getDeliveries()).thenReturn(2L);
		when(row.getApplicants()).thenReturn(2L);
		when(assignmentRepository.deliveriesByMonth(from, to)).thenReturn(List.of(row));

		List<MonthlyDeliveryResponseDto> response = assignmentServiceHandler.deliveriesByMonth(from, to);

		assertThat(response).containsExactly(
				new MonthlyDeliveryResponseDto(YearMonth.of(2026, 1), 2L, 2L));
	}
}
