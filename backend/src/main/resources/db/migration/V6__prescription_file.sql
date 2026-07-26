-- Archivo de la receta (PDF o foto) subido a Cloudflare R2.
-- Se guarda la KEY del objeto, no una URL: el bucket es privado y se sirve con presigned URLs,
-- que vencen. Persistir la URL dejaría links muertos.
ALTER TABLE prescriptions
    ADD COLUMN file_key           VARCHAR(255),
    ADD COLUMN file_content_type  VARCHAR(100),
    ADD COLUMN file_original_name VARCHAR(255);

-- Nullable a propósito: las recetas ya cargadas no tienen archivo y el operador no siempre
-- tiene el escaneo a mano. La graduación tipeada sigue siendo el dato obligatorio.
