-- Catálogo de venta de anteojos de sol (RF-28 a RF-30). Dominio autocontenido, sin relación con
-- el inventario de marcos donados (frames): acá se vende para financiar la fundación, no se dona.
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
