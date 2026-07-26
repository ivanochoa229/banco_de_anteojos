CREATE TABLE frames (
    id               BIGSERIAL PRIMARY KEY,
    donor_id         BIGINT      NOT NULL REFERENCES donors (id),
    -- Precinto físico que el operador pega sobre el marco al recibirlo (RF-10).
    seal_code        VARCHAR(30) NOT NULL UNIQUE,
    frame_type       VARCHAR(20) NOT NULL,
    material         VARCHAR(20) NOT NULL,
    lens_width_mm    INTEGER,
    bridge_width_mm  INTEGER,
    temple_length_mm INTEGER,
    status           VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    received_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT frames_frame_type_check CHECK (frame_type IN ('FULL_RIM', 'SEMI_RIMLESS', 'RIMLESS')),
    CONSTRAINT frames_material_check CHECK (material IN ('ACETATE', 'METAL', 'TITANIUM', 'PLASTIC', 'OTHER')),
    -- Ciclo de vida completo, incluido el paso por la óptica que coloca los cristales.
    -- Se define entero acá porque las migraciones son append-only: agregar un estado
    -- después obligaría a un ALTER del CHECK en una migración nueva.
    CONSTRAINT frames_status_check CHECK (status IN
        ('AVAILABLE', 'ASSIGNED', 'AT_OPTICIAN', 'READY', 'DELIVERED', 'DISCARDED'))
);

CREATE INDEX idx_frames_donor_id ON frames (donor_id);
CREATE INDEX idx_frames_status ON frames (status);
