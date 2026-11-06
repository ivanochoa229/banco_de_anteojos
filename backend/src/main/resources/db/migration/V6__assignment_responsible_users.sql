-- Auditoría de la trazabilidad (RF-16 / RNF-09): cada hito ya guardaba cuándo ocurrió, pero no
-- quién lo registró. Nullable a propósito: las filas anteriores no tienen ese dato y no hay forma
-- honesta de reconstruirlo.
ALTER TABLE assignments ADD COLUMN assigned_by_user_id BIGINT REFERENCES users (id);
ALTER TABLE assignments ADD COLUMN sent_to_optician_by_user_id BIGINT REFERENCES users (id);
ALTER TABLE assignments ADD COLUMN returned_by_user_id BIGINT REFERENCES users (id);
ALTER TABLE assignments ADD COLUMN delivered_by_user_id BIGINT REFERENCES users (id);
ALTER TABLE assignments ADD COLUMN cancelled_by_user_id BIGINT REFERENCES users (id);
