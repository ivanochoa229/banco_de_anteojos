-- Foto de la entrega: el beneficiario con sus anteojos nuevos. Vive en R2 y acá queda la key.
-- Nullable a propósito: sacar la foto es opcional y la entrega se registra igual sin ella.
ALTER TABLE assignments ADD COLUMN delivery_photo_key VARCHAR(255);
ALTER TABLE assignments ADD COLUMN delivery_photo_content_type VARCHAR(100);
ALTER TABLE assignments ADD COLUMN delivery_photo_original_name VARCHAR(255);

-- La foto muestra la cara del beneficiario: solo se le manda al donante si el beneficiario lo
-- autorizó. Queda registrado para saber, después, con qué permiso se compartió.
ALTER TABLE assignments ADD COLUMN has_delivery_photo_consent BOOLEAN NOT NULL DEFAULT false;
