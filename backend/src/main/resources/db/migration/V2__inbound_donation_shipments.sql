-- Donaciones que llegan por Vía Cargo: el donante despacha el paquete con la etiqueta de la
-- fundación ("flete a pagar en destino") y pasa el número de guía; el operador lo carga, se
-- registra en 17TRACK y las novedades entran por el mismo webhook que los envíos salientes.

-- Datos del remitente que pide la etiqueta de envío de donaciones. Nullables: los donantes que
-- entregan en mano no los informan.
ALTER TABLE donors ADD COLUMN address VARCHAR(255);
ALTER TABLE donors ADD COLUMN postal_code VARCHAR(10);
ALTER TABLE donors ADD COLUMN city VARCHAR(120);
ALTER TABLE donors ADD COLUMN province VARCHAR(60);

-- Los envíos existentes son todos salientes (entre sucursales): el DEFAULT los clasifica.
ALTER TABLE shipments ADD COLUMN direction VARCHAR(10) NOT NULL DEFAULT 'OUTBOUND';
ALTER TABLE shipments ADD COLUMN donor_id BIGINT REFERENCES donors (id);
-- Cuando el operador abrió el paquete y cargó los marcos; distinto de delivered_at, que es lo
-- que informa el carrier.
ALTER TABLE shipments ADD COLUMN received_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE shipments ALTER COLUMN origin_branch DROP NOT NULL;
ALTER TABLE shipments ALTER COLUMN destination_branch DROP NOT NULL;

ALTER TABLE shipments ADD CONSTRAINT shipments_direction_check
    CHECK (direction IN ('OUTBOUND', 'INBOUND'));
-- Saliente: entre sucursales y sin donante. Entrante: de un donante y sin sucursales.
ALTER TABLE shipments ADD CONSTRAINT shipments_direction_fields_check CHECK (
    (direction = 'OUTBOUND' AND origin_branch IS NOT NULL AND destination_branch IS NOT NULL
        AND donor_id IS NULL)
    OR (direction = 'INBOUND' AND donor_id IS NOT NULL AND origin_branch IS NULL
        AND destination_branch IS NULL));

-- RECEIVED es nuestro, como PENDING y CANCELLED: el operador confirmó la recepción.
ALTER TABLE shipments DROP CONSTRAINT shipments_status_check;
ALTER TABLE shipments ADD CONSTRAINT shipments_status_check
    CHECK (status IN ('PENDING', 'CANCELLED', 'REGISTERED', 'INFO_RECEIVED', 'IN_TRANSIT',
                      'AVAILABLE_FOR_PICKUP', 'OUT_FOR_DELIVERY', 'DELIVERED', 'DELIVERY_FAILURE',
                      'EXCEPTION', 'EXPIRED', 'NOT_FOUND', 'RECEIVED'));

CREATE INDEX idx_shipments_donor_id ON shipments (donor_id);
