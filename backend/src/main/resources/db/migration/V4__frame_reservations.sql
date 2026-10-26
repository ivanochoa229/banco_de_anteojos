-- Reserva de un marco desde el probador virtual: el beneficiario elige el que le gusta y lo saca
-- del inventario disponible por unos días, hasta que el operador se lo asigna, la cancela o vence.

ALTER TABLE frames DROP CONSTRAINT frames_status_check;
ALTER TABLE frames ADD CONSTRAINT frames_status_check CHECK (status IN
    ('AVAILABLE', 'RESERVED', 'ASSIGNED', 'AT_OPTICIAN', 'READY', 'DELIVERED', 'DISCARDED'));

-- Igual que assignments, sin columna de estado: cómo terminó la reserva lo dice cuál de los tres
-- timestamps se llenó. Activa = los tres en null.
CREATE TABLE frame_reservations (
    id           BIGSERIAL PRIMARY KEY,
    applicant_id BIGINT NOT NULL REFERENCES applicants (id),
    frame_id     BIGINT NOT NULL REFERENCES frames (id),
    reserved_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    expires_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    -- La canceló el beneficiario o el operador.
    cancelled_at TIMESTAMP WITHOUT TIME ZONE,
    -- Pasaron los días sin que se asignara: el marco volvió al inventario solo.
    expired_at   TIMESTAMP WITHOUT TIME ZONE,
    -- El operador le asignó ese marco: la reserva se concretó.
    fulfilled_at TIMESTAMP WITHOUT TIME ZONE
);

-- Una sola reserva activa por beneficiario y por marco. La regla la aplica el backend con un
-- mensaje claro; estos índices son la red si dos pedidos llegan a la vez.
CREATE UNIQUE INDEX uq_frame_reservations_active_applicant ON frame_reservations (applicant_id)
    WHERE cancelled_at IS NULL AND expired_at IS NULL AND fulfilled_at IS NULL;
CREATE UNIQUE INDEX uq_frame_reservations_active_frame ON frame_reservations (frame_id)
    WHERE cancelled_at IS NULL AND expired_at IS NULL AND fulfilled_at IS NULL;
