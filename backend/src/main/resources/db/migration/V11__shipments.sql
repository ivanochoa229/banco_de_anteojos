-- Envíos entre sucursales por Vía Cargo (RF-23 a RF-25). Un envío es un paquete con un conjunto
-- de marcos: se crea PENDING mientras se arma, al despacharlo se le carga el número de seguimiento
-- (y se registra en 17TRACK), y de ahí en más el estado lo mueve el webhook del carrier.
-- El estado de los marcos NO se toca al viajar: el inventario dice qué se puede hacer con el
-- marco, no dónde está parado (misma decisión que la cancelación de asignaciones).
CREATE TABLE shipments (
    id                  BIGSERIAL PRIMARY KEY,
    origin_branch       VARCHAR(120) NOT NULL,
    destination_branch  VARCHAR(120) NOT NULL,
    -- NULL mientras el paquete se está armando: el número lo da Vía Cargo al despachar.
    tracking_number     VARCHAR(50) UNIQUE,
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'CANCELLED', 'REGISTERED', 'INFO_RECEIVED',
                                          'IN_TRANSIT', 'AVAILABLE_FOR_PICKUP', 'OUT_FOR_DELIVERY',
                                          'DELIVERED', 'DELIVERY_FAILURE', 'EXCEPTION', 'EXPIRED',
                                          'NOT_FOUND')),
    notes               VARCHAR(500),
    cancellation_reason VARCHAR(255),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    dispatched_at       TIMESTAMP WITHOUT TIME ZONE,
    delivered_at        TIMESTAMP WITHOUT TIME ZONE
);

-- El conjunto de marcos que viaja en el paquete.
CREATE TABLE shipment_frames (
    shipment_id BIGINT NOT NULL REFERENCES shipments (id),
    frame_id    BIGINT NOT NULL REFERENCES frames (id),
    PRIMARY KEY (shipment_id, frame_id)
);

-- Historial de eventos del carrier (RF-25). El status va SIN CHECK a propósito: acá se guarda
-- el valor crudo que manda 17TRACK, y un valor nuevo del carrier no puede romper el webhook.
CREATE TABLE shipment_events (
    id          BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES shipments (id),
    status      VARCHAR(40) NOT NULL,
    description VARCHAR(500),
    location    VARCHAR(255),
    occurred_at TIMESTAMP WITHOUT TIME ZONE,
    received_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_shipment_events_shipment_id ON shipment_events (shipment_id);
