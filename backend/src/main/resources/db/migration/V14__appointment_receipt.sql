-- Bono contribución: el turno nace pendiente y se confirma cuando el beneficiario sube el
-- comprobante de la transferencia.
ALTER TABLE appointments DROP CONSTRAINT appointments_status_check;
ALTER TABLE appointments ADD CONSTRAINT appointments_status_check
    CHECK (status IN ('PENDING_PAYMENT', 'SCHEDULED', 'COMPLETED', 'MISSED', 'CANCELLED'));
ALTER TABLE appointments ALTER COLUMN status SET DEFAULT 'PENDING_PAYMENT';

ALTER TABLE appointments ADD COLUMN receipt_key VARCHAR(255);
ALTER TABLE appointments ADD COLUMN receipt_content_type VARCHAR(100);
ALTER TABLE appointments ADD COLUMN receipt_original_name VARCHAR(255);
