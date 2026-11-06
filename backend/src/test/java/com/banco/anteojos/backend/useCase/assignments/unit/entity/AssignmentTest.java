package com.banco.anteojos.backend.useCase.assignments.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.banco.anteojos.backend.business.assignments.entities.Assignment;
import com.banco.anteojos.backend.business.assignments.exception.InvalidAssignmentTransitionException;

@Tag("unit")
class AssignmentTest {

	private static final Long USER_ID = 5L;

	private Assignment assignment() {
		return new Assignment(1L, 7L, 3L, "  entrega en el turno del jueves  ", USER_ID);
	}

	private Assignment delivered() {
		Assignment assignment = assignment();
		assignment.sendToOptician(USER_ID);
		assignment.returnFromOptician(USER_ID);
		assignment.deliver(USER_ID);
		return assignment;
	}

	@Test
	void Create_Successful() {
		Assignment assignment = assignment();

		assertThat(assignment.getApplicantId()).isEqualTo(1L);
		assertThat(assignment.getFrameId()).isEqualTo(7L);
		assertThat(assignment.getPrescriptionId()).isEqualTo(3L);
		assertThat(assignment.getAssignedAt()).isNotNull();
		assertThat(assignment.getNotes()).isEqualTo("entrega en el turno del jueves");
		assertThat(assignment.isLive()).isTrue();
	}

	@Test
	void Create_WhenNotesAreBlank() {
		assertThat(new Assignment(1L, 7L, 3L, "   ", USER_ID).getNotes()).isNull();
	}

	@Test
	void FullCircuit_Successful() {
		Assignment assignment = assignment();

		assignment.sendToOptician(USER_ID);
		assertThat(assignment.getSentToOpticianAt()).isNotNull();

		assignment.returnFromOptician(USER_ID);
		assertThat(assignment.getReturnedAt()).isNotNull();

		assignment.deliver(USER_ID);
		assertThat(assignment.getDeliveredAt()).isNotNull();
		// Entregada deja de estar en curso: no es parte de la cola de trabajo del operador.
		assertThat(assignment.isLive()).isFalse();
	}

	@Test
	void SendToOptician_WhenAlreadySent() {
		Assignment assignment = assignment();
		assignment.sendToOptician(USER_ID);

		assertThatThrownBy(() -> assignment.sendToOptician(USER_ID))
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("ya fue enviado");
	}

	@Test
	void ReturnFromOptician_WhenNeverSent() {
		assertThatThrownBy(() -> assignment().returnFromOptician(USER_ID))
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("todavía no fue enviado");
	}

	@Test
	void Deliver_WhenStillAtOptician() {
		Assignment assignment = assignment();
		assignment.sendToOptician(USER_ID);

		// El marco sin los cristales puestos no le sirve al beneficiario.
		assertThatThrownBy(() -> assignment.deliver(USER_ID))
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("no volvió de la óptica");
	}

	@Test
	void Deliver_WhenNeverSentToOptician() {
		assertThatThrownBy(() -> assignment().deliver(USER_ID))
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("no volvió de la óptica");
	}

	@Test
	void Cancel_Successful() {
		Assignment assignment = assignment();

		assignment.cancel("  el beneficiario no se presentó  ", USER_ID);

		assertThat(assignment.getCancelledAt()).isNotNull();
		assertThat(assignment.getCancellationReason()).isEqualTo("el beneficiario no se presentó");
		assertThat(assignment.isLive()).isFalse();
	}

	@Test
	void Cancel_WhenAlreadyDelivered() {
		assertThatThrownBy(() -> delivered().cancel("me arrepentí", USER_ID))
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("ya fue entregada");
	}

	@Test
	void SendToOptician_WhenCancelled() {
		Assignment assignment = assignment();
		assignment.cancel("marco roto", USER_ID);

		assertThatThrownBy(() -> assignment.sendToOptician(USER_ID))
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("cancelada");
	}

	@Test
	void AttachDeliveryPhoto_Successful() {
		Assignment assignment = delivered();

		assignment.attachDeliveryPhoto("deliveries/1/abc.jpg", "image/jpeg", "entrega.jpg", true);

		assertThat(assignment.hasDeliveryPhoto()).isTrue();
		assertThat(assignment.getDeliveryPhotoOriginalName()).isEqualTo("entrega.jpg");
		assertThat(assignment.isDeliveryPhotoConsent()).isTrue();
	}

	@Test
	void AttachDeliveryPhoto_WhenNotDelivered() {
		Assignment assignment = assignment();

		assertThatThrownBy(() -> assignment.attachDeliveryPhoto("deliveries/1/abc.jpg", "image/jpeg",
				"entrega.jpg", true))
				.isInstanceOf(InvalidAssignmentTransitionException.class);
		assertThat(assignment.hasDeliveryPhoto()).isFalse();
	}

	@Test
	void Deliver_WhenAlreadyDelivered() {
		assertThatThrownBy(() -> delivered().deliver(USER_ID))
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("ya fue entregada");
	}

	@Test
	void EachMilestone_RecordsResponsibleUser() {
		Assignment assignment = new Assignment(1L, 7L, 3L, null, 10L);
		assignment.sendToOptician(11L);
		assignment.returnFromOptician(12L);
		assignment.deliver(13L);

		assertThat(assignment.getAssignedByUserId()).isEqualTo(10L);
		assertThat(assignment.getSentToOpticianByUserId()).isEqualTo(11L);
		assertThat(assignment.getReturnedByUserId()).isEqualTo(12L);
		assertThat(assignment.getDeliveredByUserId()).isEqualTo(13L);
		assertThat(assignment.getCancelledByUserId()).isNull();
	}

	@Test
	void Cancel_RecordsResponsibleUser() {
		Assignment assignment = new Assignment(1L, 7L, 3L, null, 10L);
		assignment.cancel("marco roto", 14L);

		assertThat(assignment.getCancelledByUserId()).isEqualTo(14L);
	}
}
