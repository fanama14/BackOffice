-- ============================================
-- Seed scenario pour test de planification
-- ============================================
-- Hypothese: les tables existent deja dans la base courante.

BEGIN;

TRUNCATE TABLE planification RESTART IDENTITY CASCADE;
TRUNCATE TABLE reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE distance RESTART IDENTITY CASCADE;
TRUNCATE TABLE parametre RESTART IDENTITY CASCADE;
TRUNCATE TABLE vehicule RESTART IDENTITY CASCADE;
TRUNCATE TABLE aeroport RESTART IDENTITY CASCADE;
TRUNCATE TABLE hotel RESTART IDENTITY CASCADE;
TRUNCATE TABLE lieux RESTART IDENTITY CASCADE;

-- Lieux
INSERT INTO lieux (lieu) VALUES
('aeroport'),
('hotel1'),
('hotel2');

-- Hotels (lieux_id: 2=hotel1, 3=hotel2)
INSERT INTO hotel (nom, adresse, ville, lieux_id) VALUES
('hotel1', 'adresse hotel1', 'ville1', 2),
('hotel2', 'adresse hotel2', 'ville2', 3);

-- Aeroport (lieux_id: 1=aeroport)
INSERT INTO aeroport (code, libelle, lieux_id) VALUES
('AER', 'aeroport', 1);

-- Vehicules
-- type_carburant: D=Diesel, ES=Essence
INSERT INTO vehicule (reference, nombre_place, type_carburant, heure_disponibilite) VALUES
('vehicule1', 5,  'D',  '09:00:00'),
('vehicule2', 5,  'ES', '09:00:00'),
('vehicule3', 12, 'D',  '00:00:00'),
('vehicule4', 9,  'D',  '09:00:00'),
('vehicule5', 12, 'ES', '13:00:00');

-- Parametre global
INSERT INTO parametre (temps_attente, vitesse_moyenne) VALUES
(30, 50);

-- Distances (id logique de la capture)
-- 1: aeroport -> hotel1
-- 2: aeroport -> hotel2
-- 3: hotel1   -> hotel2
-- Ajout des sens inverses pour securiser les calculs.
INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES
(1, 2, 90),
(1, 3, 35),
(2, 3, 60);

-- Reservations (aeroport_id=1, hotel_id 1=hotel1 / 2=hotel2)
INSERT INTO reservation (client_id, nombre_passager, date_arrivee, hotel_id, aeroport_id) VALUES
('client1', 7,  '2026-03-19 09:00:00', 1, 1),
('client2', 20, '2026-03-19 08:00:00', 2, 1),
('client3', 3,  '2026-03-19 09:10:00', 1, 1),
('client4', 10, '2026-03-19 09:15:00', 1, 1),
('client5', 5,  '2026-03-19 09:20:00', 1, 1),
('client6', 12, '2026-03-19 13:30:00', 1, 1);

COMMIT;
