-- Operador inicial para el equipo de trabajo de la fundación
INSERT INTO users (name, email, password_hash, role, is_active, created_at)
VALUES ('Operador Hacer Futuro', 'operador@bancodeanteojos.org',
        '$2b$10$XVAC/GNIm0bTs5UTcDLBROG7/oO2n.JuHfa/KqW5kVNB9cfNBJLra',
        'OPERATOR', true, now())
ON CONFLICT (email) DO UPDATE SET password_hash = '$2b$10$XVAC/GNIm0bTs5UTcDLBROG7/oO2n.JuHfa/KqW5kVNB9cfNBJLra', role = 'OPERATOR', is_active = true;
