-- Certificación Negativa de ANSES (RF-03/04/05). El sistema lee localmente el código de barras
-- del PDF (CUIL + nro. de transacción) y la fecha de emisión del texto; acá queda el resultado
-- ya validado. El PDF vive en R2 y se guarda la key (bucket privado, presigned URLs que vencen).
-- UNIQUE en applicant_id: hay un solo certificado vigente por solicitante; subir otro lo
-- reemplaza (la negativa vence a los 30 días, la anterior no sirve para nada).
CREATE TABLE anses_certificates (
    id                 BIGSERIAL PRIMARY KEY,
    applicant_id       BIGINT       NOT NULL UNIQUE REFERENCES applicants (id),
    cuil               VARCHAR(11)  NOT NULL,
    transaction_number VARCHAR(50)  NOT NULL,
    issue_date         DATE         NOT NULL,
    file_key           VARCHAR(255) NOT NULL,
    file_content_type  VARCHAR(100) NOT NULL,
    file_original_name VARCHAR(255) NOT NULL,
    created_at         TIMESTAMP    NOT NULL
);
