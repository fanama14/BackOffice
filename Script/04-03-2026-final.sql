

-- ============================
-- Script de création des tables
-- Date: 04-03-2026
-- ============================

-- ============================
-- Table LIEUX
-- ============================
\c postgres;
DROP DATABASE IF EXISTS sprint_4;
CREATE DATABASE sprint_4;
\c sprint_4;
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
    type_carburant VARCHAR(2) NOT NULL CHECK (type_carburant IN ('D', 'ES', 'H', 'EL'))
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
-- Données d'exemple
-- ============================

-- Lieux
INSERT INTO lieux (lieu) VALUES
('Nosy Be'),
('Antananarivo'),
('Antsiranana'),
('Fianarantsoa'),
('Aéroport Fascène'),
('Aéroport Ivato'),
('Aéroport Arrachart'),
('Aéroport Fianarantsoa');

-- Hotels
INSERT INTO hotel (nom, adresse, ville, lieux_id) VALUES
('Ocean View Hotel', 'Rue des Cocotiers', 'Nosy Be', 1),
('Capital Lodge', 'Avenue de France', 'Antananarivo', 2),
('Palm Resort', 'Plage de Ramena', 'Antsiranana', 3),
('Highland Inn', 'Route d''Andranomena', 'Fianarantsoa', 4),
('Lagoon Palace', 'Baie d''Ambatoloaka', 'Nosy Be', 1);

-- Aeroports
INSERT INTO aeroport (code, libelle, lieux_id) VALUES
('TNR', 'Aéroport International d''Ivato', 6);

-- Vehicules
INSERT INTO vehicule (reference, nombre_place, type_carburant) VALUES
('VH-001', 5, 'ES'),
('VH-002', 7, 'D'),
('VH-003', 4, 'EL'),
('VH-004', 5, 'H'),
('VH-005', 9, 'D');

-- Parametre
INSERT INTO parametre (temps_attente, vitesse_moyenne) VALUES (30, 30);

-- Distances (toutes les combinaisons entre tous les lieux)
-- 1=Nosy Be, 2=Antananarivo, 3=Antsiranana, 4=Fianarantsoa
-- 5=Aéroport Fascène, 6=Aéroport Ivato, 7=Aéroport Arrachart, 8=Aéroport Fianarantsoa
INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES

-- === Ville <-> Ville ===
(1, 2, 600),   -- Nosy Be -> Antananarivo
(2, 1, 600),   -- Antananarivo -> Nosy Be
(1, 3, 250),   -- Nosy Be -> Antsiranana
(3, 1, 250),   -- Antsiranana -> Nosy Be
(1, 4, 1000),  -- Nosy Be -> Fianarantsoa
(4, 1, 1000),  -- Fianarantsoa -> Nosy Be
(2, 3, 700),   -- Antananarivo -> Antsiranana
(3, 2, 700),   -- Antsiranana -> Antananarivo
(2, 4, 400),   -- Antananarivo -> Fianarantsoa
(4, 2, 400),   -- Fianarantsoa -> Antananarivo
(3, 4, 1100),  -- Antsiranana -> Fianarantsoa
(4, 3, 1100),  -- Fianarantsoa -> Antsiranana

-- === Aéroport <-> Ville locale ===
(5, 1, 15),    -- Aéroport Fascène -> Nosy Be
(1, 5, 15),    -- Nosy Be -> Aéroport Fascène
(6, 2, 20),    -- Aéroport Ivato -> Antananarivo
(2, 6, 20),    -- Antananarivo -> Aéroport Ivato
(7, 3, 8),     -- Aéroport Arrachart -> Antsiranana
(3, 7, 8),     -- Antsiranana -> Aéroport Arrachart
(8, 4, 12),    -- Aéroport Fianarantsoa -> Fianarantsoa
(4, 8, 12),    -- Fianarantsoa -> Aéroport Fianarantsoa

-- === Ville <-> Aéroport distant ===
(1, 6, 620),   -- Nosy Be -> Aéroport Ivato
(6, 1, 620),   -- Aéroport Ivato -> Nosy Be
(1, 7, 258),   -- Nosy Be -> Aéroport Arrachart
(7, 1, 258),   -- Aéroport Arrachart -> Nosy Be
(1, 8, 1012),  -- Nosy Be -> Aéroport Fianarantsoa
(8, 1, 1012),  -- Aéroport Fianarantsoa -> Nosy Be
(2, 5, 615),   -- Antananarivo -> Aéroport Fascène
(5, 2, 615),   -- Aéroport Fascène -> Antananarivo
(2, 7, 708),   -- Antananarivo -> Aéroport Arrachart
(7, 2, 708),   -- Aéroport Arrachart -> Antananarivo
(2, 8, 412),   -- Antananarivo -> Aéroport Fianarantsoa
(8, 2, 412),   -- Aéroport Fianarantsoa -> Antananarivo
(3, 5, 265),   -- Antsiranana -> Aéroport Fascène
(5, 3, 265),   -- Aéroport Fascène -> Antsiranana
(3, 6, 720),   -- Antsiranana -> Aéroport Ivato
(6, 3, 720),   -- Aéroport Ivato -> Antsiranana
(3, 8, 1112),  -- Antsiranana -> Aéroport Fianarantsoa
(8, 3, 1112),  -- Aéroport Fianarantsoa -> Antsiranana
(4, 5, 1015),  -- Fianarantsoa -> Aéroport Fascène
(5, 4, 1015),  -- Aéroport Fascène -> Fianarantsoa
(4, 6, 420),   -- Fianarantsoa -> Aéroport Ivato
(6, 4, 420),   -- Aéroport Ivato -> Fianarantsoa
(4, 7, 1108),  -- Fianarantsoa -> Aéroport Arrachart
(7, 4, 1108),  -- Aéroport Arrachart -> Fianarantsoa

-- === Aéroport <-> Aéroport ===
(5, 6, 635),   -- Aéroport Fascène -> Aéroport Ivato
(6, 5, 635),   -- Aéroport Ivato -> Aéroport Fascène
(5, 7, 273),   -- Aéroport Fascène -> Aéroport Arrachart
(7, 5, 273),   -- Aéroport Arrachart -> Aéroport Fascène
(5, 8, 1027),  -- Aéroport Fascène -> Aéroport Fianarantsoa
(8, 5, 1027),  -- Aéroport Fianarantsoa -> Aéroport Fascène
(6, 7, 728),   -- Aéroport Ivato -> Aéroport Arrachart
(7, 6, 728),   -- Aéroport Arrachart -> Aéroport Ivato
(6, 8, 432),   -- Aéroport Ivato -> Aéroport Fianarantsoa
(8, 6, 432),   -- Aéroport Fianarantsoa -> Aéroport Ivato
(7, 8, 1120),  -- Aéroport Arrachart -> Aéroport Fianarantsoa
(8, 7, 1120);  -- Aéroport Fianarantsoa -> Aéroport Arrachart
