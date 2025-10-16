INSERT INTO training_types (id, training_type_name) VALUES (1, 'Cardio') ON CONFLICT (id) DO NOTHING;
INSERT INTO training_types (id, training_type_name) VALUES (2, 'Strength') ON CONFLICT (id) DO NOTHING;
