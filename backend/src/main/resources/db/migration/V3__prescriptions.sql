CREATE TABLE prescriptions (
    id             BIGSERIAL PRIMARY KEY,
    applicant_id   BIGINT NOT NULL REFERENCES applicants (id),
    right_sphere   NUMERIC(4, 2),
    right_cylinder NUMERIC(4, 2),
    right_axis     INTEGER CHECK (right_axis BETWEEN 0 AND 180),
    left_sphere    NUMERIC(4, 2),
    left_cylinder  NUMERIC(4, 2),
    left_axis      INTEGER CHECK (left_axis BETWEEN 0 AND 180),
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_prescriptions_applicant_id ON prescriptions (applicant_id);
