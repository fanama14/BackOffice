


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





