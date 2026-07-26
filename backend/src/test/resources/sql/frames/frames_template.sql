-- Template de frames para tests de integración. Donante en 300000, marcos en el rango 400000.
INSERT INTO donors (id, donor_type, name, document_number, phone, email, created_at)
VALUES (300000, 'ORGANIZATION', 'Rotary Club Tucumán', '30711111119', '3814440000', 'contacto@rotary.org', now());

INSERT INTO frames (id, donor_id, seal_code, frame_type, material,
                    lens_width_mm, bridge_width_mm, temple_length_mm, status, received_at)
VALUES (400000, 300000, 'A-1001', 'FULL_RIM', 'ACETATE', 52, 18, 140, 'AVAILABLE', now()),
       (400001, 300000, 'A-1002', 'RIMLESS', 'TITANIUM', 50, 20, 145, 'DELIVERED', now());
