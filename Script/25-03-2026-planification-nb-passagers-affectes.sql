-- Migration: stocker le nombre de passagers affectes par ligne de planification
-- Objectif: conserver les splits correctement lors du mode "filtrer"

ALTER TABLE planification
ADD COLUMN IF NOT EXISTS nb_passagers_affectes INTEGER;

UPDATE planification p
SET nb_passagers_affectes = r.nombre_passager
FROM reservation r
WHERE p.reservation_id = r.id
  AND p.nb_passagers_affectes IS NULL;

ALTER TABLE planification
ALTER COLUMN nb_passagers_affectes SET NOT NULL;
