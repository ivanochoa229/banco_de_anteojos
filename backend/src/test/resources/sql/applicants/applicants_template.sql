-- Template de applicants para tests de integración. IDs de test en el rango 200000.
INSERT INTO applicants (id, first_name, last_name, dni, birth_date, phone, email, identity_validated, created_at)
VALUES (200000, 'Juana', 'Pérez', '30123456', '1985-03-12', '3815550000', 'juana.perez@mail.com', false, now());
