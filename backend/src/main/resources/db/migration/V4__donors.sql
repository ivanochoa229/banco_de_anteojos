CREATE TABLE donors (
    id              BIGSERIAL PRIMARY KEY,
    donor_type      VARCHAR(20)  NOT NULL,
    -- Nombre completo si es persona, razón social si es entidad: una sola columna evita
    -- dos pares de campos nullables que solo aplican a la mitad de las filas.
    name            VARCHAR(150) NOT NULL,
    document_number VARCHAR(20),
    phone           VARCHAR(30),
    email           VARCHAR(255),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT donors_donor_type_check CHECK (donor_type IN ('INDIVIDUAL', 'ORGANIZATION'))
);
