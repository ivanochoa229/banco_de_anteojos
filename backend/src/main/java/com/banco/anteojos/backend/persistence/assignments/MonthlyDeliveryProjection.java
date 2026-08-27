package com.banco.anteojos.backend.persistence.assignments;

import java.time.LocalDateTime;

/** Proyección de una fila de {@code deliveriesByMonth}: un mes con sus entregas y beneficiarios. */
public interface MonthlyDeliveryProjection {

	LocalDateTime getMonth();

	long getDeliveries();

	long getApplicants();
}
