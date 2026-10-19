-- Revisión humana del comprobante (RF-22): subirlo ya no confirma el turno directo, lo deja
-- pendiente de que el administrativo lo revise y recién ahí lo apruebe o lo cancele.
ALTER TABLE appointments DROP CONSTRAINT appointments_status_check;
ALTER TABLE appointments ADD CONSTRAINT appointments_status_check
    CHECK (status IN ('PENDING_PAYMENT', 'PENDING_REVIEW', 'SCHEDULED', 'COMPLETED', 'MISSED', 'CANCELLED'));
