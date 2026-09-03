-- Habilita el login de autogestión del solicitante: un tercer rol y el vínculo con su applicant.
ALTER TABLE users DROP CONSTRAINT users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'OPERATOR', 'APPLICANT'));

ALTER TABLE users ADD COLUMN applicant_id BIGINT REFERENCES applicants (id);

-- Un solicitante tiene un solo login; ADMIN/OPERATOR no usan esta columna (queda NULL).
CREATE UNIQUE INDEX idx_users_applicant_id ON users (applicant_id) WHERE applicant_id IS NOT NULL;
