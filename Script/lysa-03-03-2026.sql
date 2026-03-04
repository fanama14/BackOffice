-- ================================================================
-- Script Lysa - Mise à jour de la base de données
-- Date: 03-03-2026
-- ================================================================

-- ============================
-- Ajout de id_vehicule dans RESERVATION
-- ============================
ALTER TABLE reservation ADD COLUMN id_vehicule INTEGER;
ALTER TABLE reservation ADD CONSTRAINT fk_reservation_vehicule
    FOREIGN KEY (id_vehicule)
    REFERENCES vehicule(id)
    ON DELETE SET NULL;

-- ============================
-- Table PARAMETRE
-- ============================
CREATE TABLE parametre (
    id SERIAL PRIMARY KEY,
    temps_attente INTEGER NOT NULL DEFAULT 30,  -- en minutes (temps d'attente à l'aéroport avant départ)
    vitesse_moyenne INTEGER NOT NULL DEFAULT 30  -- en km/h
);

-- Insertion des paramètres par défaut
INSERT INTO parametre (temps_attente, vitesse_moyenne) VALUES (30, 30);

-- ============================
-- Table HOTEL - Ajout de la distance depuis l'aéroport (en km)
-- ============================
ALTER TABLE hotel ADD COLUMN distance_aeroport DECIMAL(10,2) DEFAULT 0;

-- Mise à jour des hôtels existants avec des distances
UPDATE hotel SET distance_aeroport = 15.5 WHERE nom = 'Ocean View Hotel';
UPDATE hotel SET distance_aeroport = 25.0 WHERE nom = 'Capital Lodge';
UPDATE hotel SET distance_aeroport = 12.0 WHERE nom = 'Palm Resort';
UPDATE hotel SET distance_aeroport = 45.0 WHERE nom = 'Highland Inn';
UPDATE hotel SET distance_aeroport = 8.5 WHERE nom = 'Lagoon Palace';

-- ============================
-- Données de test pour les règles d'assignation de véhicules
-- ============================

-- Supprimer les anciennes données de test si nécessaire
DELETE FROM reservation WHERE client_id LIKE 'TEST%';

-- Véhicules supplémentaires pour tester les règles d'assignation
INSERT INTO vehicule (reference, nombre_place, type_carburant) VALUES
('VH-006', 4, 'D'),      -- 4 places Diesel (priorité sur VH-003 qui est EL)
('VH-007', 4, 'ES'),     -- 4 places Essence (entre D et EL)
('VH-008', 5, 'D'),      -- 5 places Diesel (priorité sur VH-001 ES et VH-004 H)
('VH-009', 7, 'ES'),     -- 7 places Essence
('VH-010', 7, 'H'),      -- 7 places Hybride
('VH-011', 9, 'EL'),     -- 9 places Electrique
('VH-012', 12, 'D');     -- 12 places Diesel (minibus)

-- Réservations de test avec différents nombres de passagers
-- Pour tester les règles d'assignation:
-- 1. Voiture avec places >= nombre_passager
-- 2. Voiture avec places le plus proche du nombre de passagers
-- 3. Priorité: D > ES > H > EL

-- Réservations pour le 05/03/2026 (matin)
INSERT INTO reservation (client_id, nombre_passager, date_arrivee, hotel_id) VALUES
('TEST-001', 2, '2026-03-05 08:00:00', 1),  -- 2 passagers, devrait prendre VH-006 (4 places D)
('TEST-002', 4, '2026-03-05 08:30:00', 2),  -- 4 passagers, devrait prendre VH-006 (4 places D) si dispo, sinon VH-007
('TEST-003', 3, '2026-03-05 09:00:00', 3),  -- 3 passagers
('TEST-004', 5, '2026-03-05 09:30:00', 4),  -- 5 passagers, devrait prendre VH-008 (5 places D)
('TEST-005', 6, '2026-03-05 10:00:00', 5);  -- 6 passagers, devrait prendre VH-002 (7 places D)

-- Réservations pour le 05/03/2026 (après-midi)
INSERT INTO reservation (client_id, nombre_passager, date_arrivee, hotel_id) VALUES
('TEST-006', 7, '2026-03-05 14:00:00', 1),  -- 7 passagers, devrait prendre VH-002 (7 places D)
('TEST-007', 8, '2026-03-05 14:30:00', 2),  -- 8 passagers, devrait prendre VH-005 (9 places D)
('TEST-008', 10, '2026-03-05 15:00:00', 3); -- 10 passagers, devrait prendre VH-012 (12 places D)

-- Réservations pour le 06/03/2026
INSERT INTO reservation (client_id, nombre_passager, date_arrivee, hotel_id) VALUES
('TEST-009', 1, '2026-03-06 08:00:00', 1),  -- 1 passager
('TEST-010', 2, '2026-03-06 08:15:00', 2),  -- 2 passagers (peut partager avec TEST-009 si même voiture)
('TEST-011', 4, '2026-03-06 09:00:00', 3),  -- 4 passagers
('TEST-012', 5, '2026-03-06 09:30:00', 4);  -- 5 passagers

-- Réservations pour le 07/03/2026 (test de surcharge)
INSERT INTO reservation (client_id, nombre_passager, date_arrivee, hotel_id) VALUES
('TEST-013', 3, '2026-03-07 10:00:00', 1),
('TEST-014', 3, '2026-03-07 10:15:00', 2),
('TEST-015', 4, '2026-03-07 10:30:00', 3),
('TEST-016', 5, '2026-03-07 10:45:00', 4),
('TEST-017', 6, '2026-03-07 11:00:00', 5),
('TEST-018', 7, '2026-03-07 11:15:00', 1),
('TEST-019', 8, '2026-03-07 11:30:00', 2),
('TEST-020', 9, '2026-03-07 11:45:00', 3);
