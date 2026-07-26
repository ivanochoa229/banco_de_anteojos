-- Template de usuarios para tests de integración de auth.
-- IDs de test en el rango 100000. Password en texto plano: password123
INSERT INTO users (id, name, email, password_hash, role, is_active, created_at)
VALUES (100000, 'Operador Test', 'operator.test@bancoanteojos.org',
        '$2a$10$s3V6qZ1i97l8BrycgX947OuwE7q/dP/6yfAz9aTTwmPB6G6m8DLMG', 'OPERATOR', true, now());
