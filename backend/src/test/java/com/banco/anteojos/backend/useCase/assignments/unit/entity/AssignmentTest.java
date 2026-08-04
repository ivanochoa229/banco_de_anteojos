package com.banco.anteojos.backend.useCase.assignments.unit.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.banco.anteojos.backend.business.assignments.entities.Assignment;
import com.banco.anteojos.backend.business.assignments.exception.InvalidAssignmentTransitionException;

@Tag("unit")
class AssignmentTest {

	private Assignment assignment() {
		return new Assignment(1L, 7L, 3L, "  entrega en el turno del jueves  ");
	}

	private Assignment delivered() {
		Assignment assignment = assignment();
		assignment.sendToOptician();
		assignment.returnFromOptician();
		assignment.deliver();
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
		assertThat(new Assignment(1L, 7L, 3L, "   ").getNotes()).isNull();
	}

	@Test
	void FullCircuit_Successful() {
		Assignment assignment = assignment();

		assignment.sendToOptician();
		assertThat(assignment.getSentToOpticianAt()).isNotNull();

		assignment.returnFromOptician();
		assertThat(assignment.getReturnedAt()).isNotNull();

		assignment.deliver();
		assertThat(assignment.getDeliveredAt()).isNotNull();
		// Entregada deja de estar en curso: no es parte de la cola de trabajo del operador.
		assertThat(assignment.isLive()).isFalse();
	}

	@Test
	void SendToOptician_WhenAlreadySent() {
		Assignment assignment = assignment();
		assignment.sendToOptician();

		assertThatThrownBy(assignment::sendToOptician)
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("ya fue enviado");
	}

	@Test
	void ReturnFromOptician_WhenNeverSent() {
		assertThatThrownBy(assignment()::returnFromOptician)
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("todavía no fue enviado");
	}

	@Test
	void Deliver_WhenStillAtOptician() {
		Assignment assignment = assignment();
		assignment.sendToOptician();

		// El marco sin los cristales puestos no le sirve al beneficiario.
		assertThatThrownBy(assignment::deliver)
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("no volvió de la óptica");
	}

	@Test
	void Deliver_WhenNeverSentToOptician() {
		assertThatThrownBy(assignment()::deliver)
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("no volvió de la óptica");
	}

	@Test
	void Cancel_Successful() {
		Assignment assignment = assignment();

		assignment.cancel("  el beneficiario no se presentó  ");

		assertThat(assignment.getCancelledAt()).isNotNull();
		assertThat(assignment.getCancellationReason()).isEqualTo("el beneficiario no se presentó");
		assertThat(assignment.isLive()).isFalse();
	}

	@Test
	void Cancel_WhenAlreadyDelivered() {
		assertThatThrownBy(() -> delivered().cancel("me arrepentí"))
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("ya fue entregada");
	}

	@Test
	void SendToOptician_WhenCancelled() {
		Assignment assignment = assignment();
		assignment.cancel("marco roto");

		assertThatThrownBy(assignment::sendToOptician)
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("cancelada");
	}

	@Test
	void Deliver_WhenAlreadyDelivered() {
		assertThatThrownBy(delivered()::deliver)
				.isInstanceOf(InvalidAssignmentTransitionException.class)
				.hasMessageContaining("ya fue entregada");
	}
}
