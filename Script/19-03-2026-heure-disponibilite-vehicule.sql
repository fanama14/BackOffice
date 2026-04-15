-- Migration: heure de disponibilite des vehicules
-- Objectif: permettre des vehicules disponibles a partir d'une heure precise (ex: 11:00)

ALTER TABLE vehicule
ADD COLUMN IF NOT EXISTS heure_disponibilite TIME;

UPDATE vehicule
SET heure_disponibilite = '00:00:00'
WHERE heure_disponibilite IS NULL;

ALTER TABLE vehicule
ALTER COLUMN heure_disponibilite SET DEFAULT '00:00:00';

ALTER TABLE vehicule
ALTER COLUMN heure_disponibilite SET NOT NULL;

-- Exemple metier demande: V1 et V2 disponibles seulement a partir de 11:00
UPDATE vehicule
SET heure_disponibilite = '11:00:00'
WHERE reference IN ('V1', 'V2', 'VH-001', 'VH-002');
