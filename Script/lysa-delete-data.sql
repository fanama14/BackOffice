-- ================================================================
-- Script Lysa - Suppression des données de test
-- Date: 03-03-2026
-- ================================================================

-- Suppression des réservations de test
DELETE FROM reservation WHERE client_id LIKE 'TEST%';

-- Suppression des véhicules ajoutés pour les tests (VH-006 à VH-012)
DELETE FROM vehicule WHERE reference IN ('VH-006', 'VH-007', 'VH-008', 'VH-009', 'VH-010', 'VH-011', 'VH-012');

-- Réinitialisation des id_vehicule sur toutes les réservations (optionnel)
-- UPDATE reservation SET id_vehicule = NULL;

-- Note: Pour supprimer complètement les modifications de structure, exécuter:
-- ALTER TABLE reservation DROP CONSTRAINT IF EXISTS fk_reservation_vehicule;
-- ALTER TABLE reservation DROP COLUMN IF EXISTS id_vehicule;
-- ALTER TABLE hotel DROP COLUMN IF EXISTS distance_aeroport;
-- DROP TABLE IF EXISTS parametre;
