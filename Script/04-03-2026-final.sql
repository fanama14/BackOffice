

-- ============================
-- Script de création des tables
-- Date: 04-03-2026
-- ============================

-- ============================
-- Table LIEUX
-- ============================
\c postgres;
DROP DATABASE IF EXISTS sprint_5;
CREATE DATABASE sprint_5;
\c sprint_5;
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
('Antananarivo');

-- Hotels
INSERT INTO hotel (nom, adresse, ville, lieux_id) VALUES
('hotel1', 'Rue des Cocotiers', 'Nosy Be', 1);

-- Aeroports
INSERT INTO aeroport (code, libelle, lieux_id) VALUES
('TNR', 'Aéroport International d''Ivato', 2);

-- Vehicules
INSERT INTO vehicule (reference, nombre_place, type_carburant) VALUES
('VH-001', 12, 'D'),
('VH-002', 5, 'ES'),
('VH-003', 5, 'D'),
('VH-004', 12, 'ES');

-- Parametre
INSERT INTO parametre (temps_attente, vitesse_moyenne) VALUES (30, 50);

-- Distances (une seule entrée par paire, bidirectionnelle)
-- 1=Nosy Be, 2=Antananarivo, 3=Antsiranana, 4=Fianarantsoa
-- 5=Aéroport Fascène, 6=Aéroport Ivato, 7=Aéroport Arrachart, 8=Aéroport Fianarantsoa
INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES

-- === Ville <-> Ville ===
(1, 2, 50);   -- Nosy Be <-> Antananarivo

-- reservations
INSERT INTO reservation (client_id, nombre_passager, date_arrivee, hotel_id, aeroport_id) VALUES
('client1', 7,  '2026-03-12 09:00:00', 1, 1),
('client2', 11, '2026-03-12 09:00:00', 1, 1),
('client3', 3,  '2026-03-12 09:00:00', 1, 1),
('Client4', 1,  '2026-03-12 09:00:00', 1, 1),
('client5', 2,  '2026-03-12 09:00:00', 1, 1),
('client6', 20, '2026-03-12 09:00:00', 1, 1);

-- ============================
-- Table PLANIFICATION
-- ============================
CREATE TABLE planification (
    id SERIAL PRIMARY KEY,
    reservation_id INTEGER NOT NULL,
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