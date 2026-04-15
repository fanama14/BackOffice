

-- ============================
-- Script de création des tables
-- Date: 04-03-2026
-- ============================

-- ============================
-- Table LIEUX
-- ============================
\c postgres;
DROP DATABASE IF EXISTS sprint_100;
CREATE DATABASE sprint_100;
\c sprint_100;
CREATE TABLE lieux (
    id SERIAL PRIMARY KEY,
    lieu VARCHAR(150) NOT NULL
);

-- ============================
-- Table HOTEL
-- ============================
CREATE TABLE hotel (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    adresse VARCHAR(255),
    ville VARCHAR(100),
    lieux_id INTEGER NOT NULL,

    CONSTRAINT fk_hotel_lieux
        FOREIGN KEY (lieux_id)
        REFERENCES lieux(id)
        ON DELETE RESTRICT
);

-- ============================
-- Table AEROPORT
-- ============================
CREATE TABLE aeroport (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    libelle VARCHAR(200) NOT NULL,
    lieux_id INTEGER NOT NULL,

    CONSTRAINT fk_aeroport_lieux
        FOREIGN KEY (lieux_id)
        REFERENCES lieux(id)
        ON DELETE RESTRICT
);

-- ============================
-- Table VEHICULE
-- ============================
CREATE TABLE vehicule (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(100) NOT NULL UNIQUE,
    nombre_place INTEGER NOT NULL,
    type_carburant VARCHAR(2) NOT NULL CHECK (type_carburant IN ('D', 'ES', 'H', 'EL')),
    heure_disponibilite TIME NOT NULL DEFAULT '00:00:00'
);
-- D = Diesel, ES = Essence, H = Hybride, EL = Electrique

-- ============================
-- Table PARAMETRE
-- ============================
CREATE TABLE parametre (
    id SERIAL PRIMARY KEY,
    temps_attente INTEGER NOT NULL,     -- en minutes
    vitesse_moyenne INTEGER NOT NULL    -- en km/h
);

-- ============================
-- Table DISTANCE
-- ============================
CREATE TABLE distance (
    id SERIAL PRIMARY KEY,
    lieux_from INTEGER NOT NULL,
    lieux_to INTEGER NOT NULL,
    valeur DOUBLE PRECISION NOT NULL,   -- distance en km

    CONSTRAINT fk_distance_from
        FOREIGN KEY (lieux_from)
        REFERENCES lieux(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_distance_to
        FOREIGN KEY (lieux_to)
        REFERENCES lieux(id)
        ON DELETE CASCADE
);

-- ============================
-- Table RESERVATION
-- ============================
CREATE TABLE reservation (
    id SERIAL PRIMARY KEY,
    client_id VARCHAR(100),
    nombre_passager INTEGER,
    date_arrivee TIMESTAMP NOT NULL,
    hotel_id INTEGER NOT NULL,
    aeroport_id INTEGER NOT NULL,

    CONSTRAINT fk_reservation_hotel
        FOREIGN KEY (hotel_id)
        REFERENCES hotel(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_reservation_aeroport
        FOREIGN KEY (aeroport_id)
        REFERENCES aeroport(id)
        ON DELETE CASCADE
);

-- ============================
-- Table PLANIFICATION
-- ============================
CREATE TABLE planification (
    id SERIAL PRIMARY KEY,
    reservation_id INTEGER NOT NULL,
    nb_passagers_affectes INTEGER NOT NULL,
    vehicule_id INTEGER,
    groupe_id INTEGER NOT NULL,
    ordre_livraison INTEGER,
    heure_depart TIMESTAMP,
    heure_retour TIMESTAMP,

    CONSTRAINT fk_planification_reservation
        FOREIGN KEY (reservation_id)
        REFERENCES reservation(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_planification_vehicule
        FOREIGN KEY (vehicule_id)
        REFERENCES vehicule(id)
        ON DELETE SET NULL
);

-- ============================
-- Donnees d'exemple pour tester la regle 4
-- VEHICULE AVEC LE MOINS DE TRAJETS
-- ============================

-- Lieux
INSERT INTO lieux (lieu) VALUES
('Aeroport Ivato'),
('Hotel Zone A'),
('Hotel Zone B');

-- Hotels (distances differentes depuis l'aeroport)
INSERT INTO hotel (nom, adresse, ville, lieux_id) VALUES
('Hotel Alpha', 'Lot A1', 'Antananarivo', 2),
('Hotel Bravo', 'Lot B2', 'Antananarivo', 3);

-- Aeroports
INSERT INTO aeroport (code, libelle, lieux_id) VALUES
('TNR', 'Aeroport International d''Ivato', 1);

-- Vehicules (jeu reinitialise pour scenario regle 4)
-- Uniquement diesel et essence, avec plus de vehicules pour tester le remplissage.
INSERT INTO vehicule (reference, nombre_place, type_carburant, heure_disponibilite) VALUES
('VH-001', 6, 'D', '11:00:00'),
('VH-002', 6, 'ES', '11:00:00'),
('VH-003', 10, 'D', '00:00:00'),
('VH-004', 10, 'ES', '00:00:00'),
('VH-005', 4, 'D', '00:00:00'),
('VH-006', 4, 'ES', '00:00:00');

-- Parametre
INSERT INTO parametre (temps_attente, vitesse_moyenne) VALUES (30, 40);

-- Distances (une seule entree par paire, bidirectionnelle)
-- 1=Aeroport Ivato, 2=Hotel Zone A, 3=Hotel Zone B
INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES
-- Aeroport -> hotels (distances volontairement differentes)
(1, 2, 8.0),
(1, 3, 20.0),
-- Liaison entre les 2 hotels
(2, 3, 13.0);

-- Reservations
-- Scenario regle 4 SANS planifications pre-remplies:
-- - Les fenetres sont de 30 min.
-- - Les trajets sont construits pendant la planification de la journee.
-- - TEST-R4-001 arrive plus tard, quand VH-001 et VH-002 sont tous deux adequats,
--   mais avec un nombre de trajets different (VH-001=2, VH-002=1) :
--   la regle 4 doit choisir VH-002.
-- - Plusieurs clients 1 passager permettent de verifier la regle 2 (remplir vehicule).
INSERT INTO reservation (client_id, nombre_passager, date_arrivee, hotel_id, aeroport_id) VALUES
('W1-CLIENT-01', 5, '2026-03-19 06:00:00', 1, 1),
('W1-CLIENT-02', 1, '2026-03-19 06:20:00', 2, 1),
('W1-CLIENT-03', 4, '2026-03-19 06:25:00', 2, 1),

('W2-CLIENT-01', 5, '2026-03-19 07:30:00', 2, 1),
('W2-CLIENT-02', 1, '2026-03-19 07:40:00', 1, 1),
('W2-CLIENT-03', 8, '2026-03-19 07:50:00', 1, 1),

('W3-CLIENT-01', 6, '2026-03-19 08:20:00', 1, 1),
('W3-CLIENT-02', 4, '2026-03-19 08:30:00', 2, 1),

('TEST-R4-001', 5, '2026-03-19 09:30:00', 2, 1),
('TEST-R4-002', 1, '2026-03-19 09:35:00', 1, 1);