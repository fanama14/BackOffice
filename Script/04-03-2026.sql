-- ============================
-- Script de création des tables
-- Date: 04-03-2026
-- ============================

-- ============================
-- Table LIEUX
-- ============================

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

-- Distances (entre aéroports et villes)
INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES
(5, 1, 15),    -- Aéroport Fascène -> Nosy Be : 15 km
(6, 2, 20),    -- Aéroport Ivato -> Antananarivo : 20 km
(7, 3, 8),     -- Aéroport Arrachart -> Antsiranana : 8 km
(8, 4, 12);    -- Aéroport Fianarantsoa -> Fianarantsoa : 12 km
