-- Catálogo de anteojos de sol como unidades únicas: cada producto es un anteojo físico que se
-- vende una sola vez. Deja de haber stock; vender pasa el producto a SOLD.

ALTER TABLE products DROP CONSTRAINT products_status_check;
ALTER TABLE products ADD CONSTRAINT products_status_check CHECK (status IN
    ('ACTIVE', 'SOLD', 'DISCONTINUED'));

-- Un producto con alguna venta ya se vendió. Si le quedaba stock, esas unidades se cargan de
-- nuevo como productos nuevos: no hay forma de separarlas en anteojos distintos automáticamente.
UPDATE products SET status = 'SOLD'
WHERE status = 'ACTIVE' AND id IN (SELECT product_id FROM product_sales);

ALTER TABLE products DROP COLUMN stock_quantity;

-- Una venta es de un solo anteojo: sin cantidad ni total. price sigue siendo la copia del precio
-- al momento de vender, para que editar el producto después no reescriba la venta.
ALTER TABLE product_sales DROP COLUMN quantity;
ALTER TABLE product_sales DROP COLUMN total_amount;
ALTER TABLE product_sales RENAME COLUMN unit_price TO price;

-- Una venta por producto. La regla la aplica el backend (lock + estado SOLD); el índice es la red
-- si dos ventas llegan a la vez. Reemplaza al índice no único de V1.
DROP INDEX idx_product_sales_product_id;
CREATE UNIQUE INDEX uq_product_sales_product_id ON product_sales (product_id);
