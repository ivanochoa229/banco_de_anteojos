-- Días de atención de la sede (RF-20). La fundación no atiende todos los días: el personal habilita
-- cada fecha (típicamente un viernes cada dos semanas, a veces dos seguidos) con su horario y su
-- cupo, y los turnos solo se pueden pedir sobre esos días.
--
-- Se guarda inicio + duración + cantidad y no una hora de fin: así subir el cupo agrega franjas al
-- final sin correr las horas que ya se le dieron a los beneficiarios.
CREATE TABLE appointment_days (
    id                    BIGSERIAL PRIMARY KEY,
    attention_date        DATE NOT NULL UNIQUE,
    start_time            TIME NOT NULL,
    slot_duration_minutes INTEGER NOT NULL CHECK (slot_duration_minutes > 0),
    slot_count            INTEGER NOT NULL CHECK (slot_count > 0),
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- NULL para los turnos anteriores a esta migración, que se agendaron con fecha libre. Si se borra
-- un día solo pueden quedar colgando turnos cancelados (el backend no deja borrarlo con turnos
-- activos): esos pierden la referencia pero conservan su fecha.
ALTER TABLE appointments ADD COLUMN appointment_day_id BIGINT
    REFERENCES appointment_days (id) ON DELETE SET NULL;

CREATE INDEX idx_appointments_appointment_day_id ON appointments (appointment_day_id);

-- Red de seguridad contra el doble booking de una franja: la reserva ya bloquea el día con
-- SELECT ... FOR UPDATE, pero la base es la última palabra. Los cancelados liberan la franja.
CREATE UNIQUE INDEX uq_appointments_active_slot ON appointments (scheduled_at)
    WHERE appointment_day_id IS NOT NULL AND status <> 'CANCELLED';
