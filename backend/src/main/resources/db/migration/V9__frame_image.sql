-- Foto del marco para el probador virtual (RF-18): se superpone sobre la foto del beneficiario.
-- Se guarda la KEY del objeto, no una URL: el bucket es privado y se sirve con presigned URLs,
-- que vencen. Persistir la URL dejaría links muertos.
ALTER TABLE frames
    ADD COLUMN image_key           VARCHAR(255),
    ADD COLUMN image_content_type  VARCHAR(100),
    ADD COLUMN image_original_name VARCHAR(255);

-- Nullable a propósito: los marcos ya cargados no tienen foto y sacarle una recortada a cada
-- donación es trabajo aparte. El probador solo ofrece los marcos que sí la tienen.
