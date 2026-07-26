CREATE TABLE applicants (
    id                 BIGSERIAL PRIMARY KEY,
    first_name         VARCHAR(100) NOT NULL,
    last_name          VARCHAR(100) NOT NULL,
    dni                VARCHAR(8)   NOT NULL UNIQUE,
    cuil               VARCHAR(11),
    birth_date         DATE,
    phone              VARCHAR(30),
    email              VARCHAR(255),
    identity_validated BOOLEAN      NOT NULL DEFAULT false,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);
