-- ============================
-- Table HOTEL
-- ============================
CREATE TABLE hotel (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    adresse VARCHAR(255),
    ville VARCHAR(100),
    telephone VARCHAR(30)
);

-- ============================
-- Table RESERVATION
-- ============================

CREATE TABLE reservation (
    id SERIAL PRIMARY KEY,

    client_id VARCHAR(100) , -- pas foreign key

    nombre_passager INTEGER ,

    date_arrivee TIMESTAMP NOT NULL,

    hotel_id INTEGER NOT NULL,

    CONSTRAINT fk_reservation_hotel
        FOREIGN KEY (hotel_id)
        REFERENCES hotel(id)
        ON DELETE CASCADE
);

INSERT INTO hotel (nom, adresse, ville, telephone) VALUES
('Ocean View Hotel', 'Rue des Cocotiers', 'Nosy Be', '+261341111111'),
('Capital Lodge', 'Avenue de France', 'Antananarivo',  '+261320222222'),
('Palm Resort', 'Plage de Ramena', 'Antsiranana',  '+261380333333'),
('Highland Inn', 'Route d Andranomena', 'Fianarantsoa',  '+261340444444'),
('Lagoon Palace', 'Baie d Ambatoloaka', 'Nosy Be',  '+261381555555');

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

INSERT INTO vehicule (reference, nombre_place, type_carburant) VALUES
('VH-001', 5, 'ES'),
('VH-002', 7, 'D'),
('VH-003', 4, 'EL'),
('VH-004', 5, 'H'),
('VH-005', 9, 'D');