-- Asignación de un marco a un beneficiario y su recorrido hasta la entrega (RF-14 a RF-16).
-- No hay columna de estado: cada hito del circuito es un timestamp, que además ES la
-- trazabilidad. Duplicar acá el estado del marco obligaría a mantener dos máquinas de estado
-- sincronizadas; la fuente de verdad de "dónde está el marco" es frames.status.
CREATE TABLE assignments (
    id                  BIGSERIAL PRIMARY KEY,
    applicant_id        BIGINT NOT NULL REFERENCES applicants (id),
    frame_id            BIGINT NOT NULL REFERENCES frames (id),
    -- La receta viaja con el marco a la óptica: sin ella no se pueden colocar los cristales.
    prescription_id     BIGINT NOT NULL REFERENCES prescriptions (id),
    assigned_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    sent_to_optician_at TIMESTAMP WITHOUT TIME ZONE,
    returned_at         TIMESTAMP WITHOUT TIME ZONE,
    delivered_at        TIMESTAMP WITHOUT TIME ZONE,
    cancelled_at        TIMESTAMP WITHOUT TIME ZONE,
    cancellation_reason VARCHAR(255),
    notes               VARCHAR(500)
);

-- Un marco entra en una sola asignación no cancelada. El invariante lo garantiza la DB y no una
-- consulta previa, que sería una condición de carrera entre dos operadores.
-- El predicado incluye a las entregadas a propósito: un marco entregado no se reasigna. Eso hoy
-- también lo impide Frame.returnToInventory(), que no deja volver a AVAILABLE algo DELIVERED;
-- si alguna vez se admite reingresar un marco ya entregado, hay que revisar este índice.
CREATE UNIQUE INDEX idx_assignments_live_frame ON assignments (frame_id) WHERE cancelled_at IS NULL;

CREATE INDEX idx_assignments_applicant_id ON assignments (applicant_id);
