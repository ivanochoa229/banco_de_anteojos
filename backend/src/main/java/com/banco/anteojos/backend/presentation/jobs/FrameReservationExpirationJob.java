package com.banco.anteojos.backend.presentation.jobs;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.banco.anteojos.backend.orchestrator.frameReservations.FrameReservationUseCaseOrchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Punto de entrada programado, como un controller pero sin request: devuelve al inventario los
 * marcos de las reservas vencidas. Cada 15 minutos alcanza: un marco puede quedar reservado un
 * rato de más, pero nunca días.
 *
 * Se apaga con {@code scheduling.enabled=false} (los tests de integración lo hacen, para que la
 * tarea no corra en el medio de un test).
 */
@Component
@ConditionalOnProperty(name = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class FrameReservationExpirationJob {

	private final FrameReservationUseCaseOrchestrator frameReservationOrchestrator;

	@Scheduled(cron = "0 */15 * * * *")
	public void expireOverdueReservations() {
		// Un fallo acá no tiene a quién mostrarse: va al log y se reintenta en la próxima vuelta.
		try {
			frameReservationOrchestrator.expireOverdueReservations();
		} catch (RuntimeException e) {
			log.error("No se pudieron vencer las reservas de marcos", e);
		}
	}
}
