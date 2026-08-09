-- Turnos de atención de los beneficiarios (RF-20 a RF-22). A diferencia de assignments, acá sí
-- hay columna de estado: un turno no es trazabilidad de un circuito sino una agenda, y su historia
-- (reprogramaciones) no interesa fila por fila — interesa en qué quedó.
CREATE TABLE appointments (
    id                  BIGSERIAL PRIMARY KEY,
    applicant_id        BIGINT NOT NULL REFERENCES applicants (id),
    -- Turno de retiro: apunta al par de anteojos que se viene a buscar. NULL = atención general.
    assignment_id       BIGINT REFERENCES assignments (id),
    scheduled_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
                        CHECK (status IN ('SCHEDULED', 'COMPLETED', 'MISSED', 'CANCELLED')),
    notes               VARCHAR(500),
    cancellation_reason VARCHAR(255),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_appointments_applicant_id ON appointments (applicant_id);
-- La consulta típica es la agenda de un día: filtra por rango de scheduled_at.
CREATE INDEX idx_appointments_scheduled_at ON appointments (scheduled_at);
