-- Schema inicial del Banco de Anteojos. Consolida las migraciones V1–V17 del desarrollo en una
-- sola, antes del primer deploy (no había nada en producción). De acá en adelante, append-only.
--
-- Los archivos (recetas, certificados ANSES, fotos, comprobantes) viven en Cloudflare R2 y acá se
-- guarda la KEY del objeto, no una URL: el bucket es privado y se sirve con presigned URLs, que
-- vencen. Persistir la URL dejaría links muertos.

-- ---------------------------------------------------------------------------------------------
-- Solicitantes / beneficiarios
-- ---------------------------------------------------------------------------------------------
CREATE TABLE applicants (
    id                 BIGSERIAL PRIMARY KEY,
    first_name         VARCHAR(100) NOT NULL,
    last_name          VARCHAR(100) NOT NULL,
    dni                VARCHAR(8)   NOT NULL UNIQUE,
    cuil               VARCHAR(11),
    birth_date         DATE,
    phone              VARCHAR(30),
    email              VARCHAR(255),
    identity_validated BOOLEAN      NOT NULL DEFAULT false,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------------------------
-- Seguridad. Sin usuarios sembrados: el primer ADMIN lo crea AdminBootstrap desde la
-- configuración, y los operadores de producción los da de alta el DBA.
-- ---------------------------------------------------------------------------------------------
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'OPERATOR', 'APPLICANT')),
    is_active     BOOLEAN      NOT NULL DEFAULT true,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    -- Login de autogestión del solicitante; ADMIN/OPERATOR no usan esta columna (queda NULL).
    applicant_id  BIGINT REFERENCES applicants (id)
);

-- Un solicitante tiene un solo login.
CREATE UNIQUE INDEX idx_users_applicant_id ON users (applicant_id) WHERE applicant_id IS NOT NULL;

-- ---------------------------------------------------------------------------------------------
-- Recetas
-- ---------------------------------------------------------------------------------------------
CREATE TABLE prescriptions (
    id                 BIGSERIAL PRIMARY KEY,
    applicant_id       BIGINT NOT NULL REFERENCES applicants (id),
    right_sphere       NUMERIC(4, 2),
    right_cylinder     NUMERIC(4, 2),
    right_axis         INTEGER CHECK (right_axis BETWEEN 0 AND 180),
    left_sphere        NUMERIC(4, 2),
    left_cylinder      NUMERIC(4, 2),
    left_axis          INTEGER CHECK (left_axis BETWEEN 0 AND 180),
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    -- Archivo de la receta (PDF o foto) en R2. Nullable a propósito: el operador no siempre tiene
    -- el escaneo a mano. La graduación tipeada sigue siendo el dato obligatorio.
    file_key           VARCHAR(255),
    file_content_type  VARCHAR(100),
    file_original_name VARCHAR(255)
);

CREATE INDEX idx_prescriptions_applicant_id ON prescriptions (applicant_id);

-- Certificación Negativa de ANSES (RF-03/04/05). El sistema lee localmente el código de barras
-- del PDF (CUIL + nro. de transacción) y la fecha de emisión del texto; acá queda el resultado
-- ya validado. El PDF vive en R2.
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

-- ---------------------------------------------------------------------------------------------
-- Donantes e inventario de marcos
-- ---------------------------------------------------------------------------------------------
CREATE TABLE donors (
    id              BIGSERIAL PRIMARY KEY,
    donor_type      VARCHAR(20)  NOT NULL,
    -- Nombre completo si es persona, razón social si es entidad: una sola columna evita
    -- dos pares de campos nullables que solo aplican a la mitad de las filas.
    name            VARCHAR(150) NOT NULL,
    document_number VARCHAR(20),
    phone           VARCHAR(30),
    email           VARCHAR(255),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT donors_donor_type_check CHECK (donor_type IN ('INDIVIDUAL', 'ORGANIZATION'))
);

CREATE TABLE frames (
    id                  BIGSERIAL PRIMARY KEY,
    donor_id            BIGINT      NOT NULL REFERENCES donors (id),
    -- Precinto físico que el operador pega sobre el marco al recibirlo (RF-10).
    seal_code           VARCHAR(30) NOT NULL UNIQUE,
    frame_type          VARCHAR(20) NOT NULL,
    material            VARCHAR(20) NOT NULL,
    lens_width_mm       INTEGER,
    bridge_width_mm     INTEGER,
    temple_length_mm    INTEGER,
    status              VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    received_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    -- Foto del marco para el probador virtual (RF-18), en R2. Nullable a propósito: sacarle una
    -- foto recortada a cada donación es trabajo aparte; el probador solo ofrece los que la tienen.
    image_key           VARCHAR(255),
    image_content_type  VARCHAR(100),
    image_original_name VARCHAR(255),
    CONSTRAINT frames_frame_type_check CHECK (frame_type IN ('FULL_RIM', 'SEMI_RIMLESS', 'RIMLESS')),
    CONSTRAINT frames_material_check CHECK (material IN ('ACETATE', 'METAL', 'TITANIUM', 'PLASTIC', 'OTHER')),
    -- Ciclo de vida completo, incluido el paso por la óptica que coloca los cristales.
    CONSTRAINT frames_status_check CHECK (status IN
        ('AVAILABLE', 'ASSIGNED', 'AT_OPTICIAN', 'READY', 'DELIVERED', 'DISCARDED'))
);

CREATE INDEX idx_frames_donor_id ON frames (donor_id);
CREATE INDEX idx_frames_status ON frames (status);

-- ---------------------------------------------------------------------------------------------
-- Asignaciones (match receta ↔ marco) y trazabilidad
-- ---------------------------------------------------------------------------------------------
-- Asignación de un marco a un beneficiario y su recorrido hasta la entrega (RF-14 a RF-16).
-- No hay columna de estado: cada hito del circuito es un timestamp, que además ES la
-- trazabilidad. Duplicar acá el estado del marco obligaría a mantener dos máquinas de estado
-- sincronizadas; la fuente de verdad de "dónde está el marco" es frames.status.
CREATE TABLE assignments (
    id                  BIGSERIAL PRIMARY KEY,
    applicant_id        BIGINT NOT NULL REFERENCES applicants (id),
    frame_id            BIGINT NOT NULL REFERENCES frames (id),
    -- La receta viaja con el marco a la óptica: sin ella no se pueden colocar los cristales.
    prescription_id     BIGINT NOT NULL REFERENCES prescriptions (id),
    assigned_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    sent_to_optician_at TIMESTAMP WITHOUT TIME ZONE,
    returned_at         TIMESTAMP WITHOUT TIME ZONE,
    delivered_at        TIMESTAMP WITHOUT TIME ZONE,
    cancelled_at        TIMESTAMP WITHOUT TIME ZONE,
    cancellation_reason VARCHAR(255),
    notes               VARCHAR(500)
);

-- Un marco entra en una sola asignación no cancelada. El invariante lo garantiza la DB y no una
-- consulta previa, que sería una condición de carrera entre dos operadores.
-- El predicado incluye a las entregadas a propósito: un marco entregado no se reasigna. Eso hoy
-- también lo impide Frame.returnToInventory(), que no deja volver a AVAILABLE algo DELIVERED;
-- si alguna vez se admite reingresar un marco ya entregado, hay que revisar este índice.
CREATE UNIQUE INDEX idx_assignments_live_frame ON assignments (frame_id) WHERE cancelled_at IS NULL;

CREATE INDEX idx_assignments_applicant_id ON assignments (applicant_id);

-- ---------------------------------------------------------------------------------------------
-- Turnos
-- ---------------------------------------------------------------------------------------------
-- Días de atención de la sede (RF-20). La fundación no atiende todos los días: el personal habilita
-- cada fecha (típicamente un viernes cada dos semanas, a veces dos seguidos) con su horario y su
-- cupo, y los turnos solo se pueden pedir sobre esos días.
--
-- Se guarda inicio + duración + cantidad y no una hora de fin: así subir el cupo agrega franjas al
-- final sin correr las horas que ya se le dieron a los beneficiarios.
CREATE TABLE appointment_days (
    id                    BIGSERIAL PRIMARY KEY,
    attention_date        DATE NOT NULL UNIQUE,
    start_time            TIME NOT NULL,
    slot_duration_minutes INTEGER NOT NULL CHECK (slot_duration_minutes > 0),
    slot_count            INTEGER NOT NULL CHECK (slot_count > 0),
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- Turnos de atención de los beneficiarios (RF-20 a RF-22). A diferencia de assignments, acá sí
-- hay columna de estado: un turno no es trazabilidad de un circuito sino una agenda, y su historia
-- (reprogramaciones) no interesa fila por fila — interesa en qué quedó.
--
-- Bono contribución: el turno nace PENDING_PAYMENT; el beneficiario sube el comprobante de la
-- transferencia y queda PENDING_REVIEW hasta que el administrativo lo aprueba (SCHEDULED) o lo
-- cancela (RF-22).
CREATE TABLE appointments (
    id                    BIGSERIAL PRIMARY KEY,
    applicant_id          BIGINT NOT NULL REFERENCES applicants (id),
    -- Turno de retiro: apunta al par de anteojos que se viene a buscar. NULL = atención general.
    assignment_id         BIGINT REFERENCES assignments (id),
    scheduled_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status                VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT'
                          CHECK (status IN ('PENDING_PAYMENT', 'PENDING_REVIEW', 'SCHEDULED', 'COMPLETED',
                                            'MISSED', 'CANCELLED')),
    notes                 VARCHAR(500),
    cancellation_reason   VARCHAR(255),
    created_at            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    -- Comprobante de la transferencia, en R2.
    receipt_key           VARCHAR(255),
    receipt_content_type  VARCHAR(100),
    receipt_original_name VARCHAR(255),
    -- Si se borra un día solo pueden quedar colgando turnos cancelados (el backend no deja borrarlo
    -- con turnos activos): esos pierden la referencia pero conservan su fecha.
    appointment_day_id    BIGINT REFERENCES appointment_days (id) ON DELETE SET NULL
);

CREATE INDEX idx_appointments_applicant_id ON appointments (applicant_id);
-- La consulta típica es la agenda de un día: filtra por rango de scheduled_at.
CREATE INDEX idx_appointments_scheduled_at ON appointments (scheduled_at);
CREATE INDEX idx_appointments_appointment_day_id ON appointments (appointment_day_id);

-- Red de seguridad contra el doble booking de una franja: la reserva ya bloquea el día con
-- SELECT ... FOR UPDATE, pero la base es la última palabra. Los cancelados liberan la franja.
CREATE UNIQUE INDEX uq_appointments_active_slot ON appointments (scheduled_at)
    WHERE appointment_day_id IS NOT NULL AND status <> 'CANCELLED';

-- ---------------------------------------------------------------------------------------------
-- Logística (Vía Cargo + 17TRACK)
-- ---------------------------------------------------------------------------------------------
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

-- ---------------------------------------------------------------------------------------------
-- Catálogo de venta de anteojos de sol
-- ---------------------------------------------------------------------------------------------
-- RF-28 a RF-30. Dominio autocontenido, sin relación con el inventario de marcos donados
-- (frames): acá se vende para financiar la fundación, no se dona.
CREATE TABLE products (
    id                   BIGSERIAL PRIMARY KEY,
    name                 VARCHAR(150) NOT NULL,
    description          VARCHAR(500),
    price                NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    stock_quantity       INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    status               VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                         CHECK (status IN ('ACTIVE', 'DISCONTINUED')),
    image_key            VARCHAR(255),
    image_content_type   VARCHAR(100),
    image_original_name  VARCHAR(255),
    created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

-- Historial de ventas (RF-29). unit_price copia el precio al momento de vender: si el precio
-- del producto cambia después, no debe reescribir ventas ya cerradas.
CREATE TABLE product_sales (
    id           BIGSERIAL PRIMARY KEY,
    product_id   BIGINT NOT NULL REFERENCES products (id),
    quantity     INTEGER NOT NULL CHECK (quantity > 0),
    unit_price   NUMERIC(10, 2) NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,
    buyer_name   VARCHAR(150),
    notes        VARCHAR(500),
    sold_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_product_sales_product_id ON product_sales (product_id);
