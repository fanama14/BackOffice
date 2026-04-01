-- ============================================
-- Scenario Sprint 8
-- Objectif:
-- 1) Reinitialiser toutes les donnees
-- 2) Illustrer TA=30 min, 2 vehicules (13 et 6 places), 4 clients, 2 hotels
-- 3) Montrer les reliquats non assignes (C1 reste 11, C2 reste 3, C3 reste 2)
-- 4) Montrer les regles:
--    - priorite aux reliquats non assignes
--    - vehicule vide: ordre desc nb passagers
--    - remplissage des places restantes: nb passagers le plus proche
--    - TA de reservation: attente du dernier horaire de reservation de la fenetre
--    - TA de vehicule revenu: depart des vehicules plein/non-plein sans attendre les autres
-- ============================================

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
('Aeroport Sprint8'),
('Hotel Alpha'),
('Hotel Beta');

-- Hotels (lieux_id: 2 et 3)
INSERT INTO hotel (nom, adresse, ville, lieux_id) VALUES
('Hotel Alpha', 'Adresse Alpha', 'Antananarivo', 2),
('Hotel Beta',  'Adresse Beta',  'Antananarivo', 3);

-- Aeroport (lieux_id: 1)
INSERT INTO aeroport (code, libelle, lieux_id) VALUES
('TNR', 'Ivato Sprint8', 1);

-- Vehicules demandes
INSERT INTO vehicule (reference, nombre_place, type_carburant, heure_disponibilite) VALUES
('V1', 13, 'D',  '00:00:00'),
('V2',  6, 'ES', '00:00:00');

-- Parametres globaux
-- TA = 30 minutes
-- Vitesse moyenne = 60 km/h
INSERT INTO parametre (temps_attente, vitesse_moyenne) VALUES
(30, 60);

-- Distances bidirectionnelles
-- 1=Aeroport, 2=Hotel Alpha, 3=Hotel Beta
INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES
(1, 2, 12),
(2, 1, 12),
(1, 3, 18),
(3, 1, 18),
(2, 3, 10),
(3, 2, 10);

-- Reservations (4 clients)
-- C4: trajet initial pour montrer un vehicule deja revenu avant la fenetre principale
INSERT INTO reservation (client_id, nombre_passager, date_arrivee, hotel_id, aeroport_id) VALUES
('C4',  4, '2026-03-26 09:00:00', 2, 1),

-- Fenetre principale (TA reservation 10:00-10:30)
-- C1/C2/C3 provoquent les reliquats demandes:
-- - C1: 24 -> 13 affectes sur V1 => reste 11
-- - C2:  9 ->  6 affectes sur V2 => reste 3
-- - C3:  2 -> pas de vehicule libre dans cette fenetre => reste 2
('C1', 24, '2026-03-26 10:00:00', 1, 1),
('C2',  9, '2026-03-26 10:05:00', 2, 1),
('C3',  2, '2026-03-26 10:10:00', 1, 1);

COMMIT;

-- ============================================
-- Lecture attendue apres execution de la planification:
--
-- 1) TA reservation 10:00-10:30
--    - V1 prend C1(13), V2 prend C2(6), C1 reste11, C2 reste3, C3 reste2.
--    - Comme TA de reservation: les vehicules attendent la derniere reservation
--      de la fenetre (10:10) avant depart.
--
-- 2) Retours attendus (avec distances ci-dessus)
--    - V1: hotel1 aller-retour = 24 km => retour ~10:34
--    - V2: hotel2 aller-retour = 36 km => retour ~10:46
--    => TA vehicule revenu: 10:34-11:04 (cree par le premier retour)
--
-- 3) Dans TA vehicule revenu
--    - Priorite reliquats: C1(11), C2(3), C3(2)
--    - V1 revenu a 10:34 prend C1(11), reste 2 places.
--      Remplissage par proximite: C3(2) est le plus proche de 2 => V1 plein.
--      V1 part directement a 10:34 (vehicule revenu plein).
--    - V2 revenu a 10:46 prend C2(3), il reste 3 places mais plus de client.
--      V2 part aussi a 10:46 sans attendre (vehicule revenu non plein).
--
-- Le scenario montre donc:
-- - priorite des restes non assignes
-- - ordre desc sur vehicule vide
-- - meilleure proximite pour places restantes
-- - depart immediat (TA de vehicule revenu) pour vehicule plein ET non plein
-- - pas de chevauchement des TA grace au prochain ancrage apres la fenetre courante
-- ============================================
