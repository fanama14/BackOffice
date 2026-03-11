# Système de Regroupement de Réservations

## Vue d'ensemble

Ce système implémente la fonctionnalité de regroupement de réservations selon les règles métier suivantes:

### Règles de Regroupement

1. **Possibilité de regroupement**: Les réservations peuvent être groupées si elles partagent le même vol (même date/heure d'arrivée)

2. **Capacité du véhicule**: 
   - ✓ Exemple valide: Réservation de 4 personnes + Réservation de 2 personnes = 6 personnes dans un véhicule 7 places
   - Le total des passagers doit être inférieur (pas égal) à la capacité du véhicule

3. **Clients indivisibles**: 
   - Les groupes de clients ne peuvent pas être séparés
   - Exemple: Réservation de 4 + Réservation de 3 ne peuvent pas être dans un véhicule 7 places (4+3=7)

4. **Ordre de visite**: 
   - Les hôtels sont visités dans l'ordre de distance (le plus proche en premier)
   - Exemple: Ivato → Ibis (40km) → Colbert (60km) → Ivato
   - Distance totale: 40 + 20 + 60 = 120km

5. **Distances égales**: 
   - Si deux destinations ont la même distance, l'ordre alphabétique est utilisé
   - Exemple: Ankadikely et Tsiazompaniry (tous deux à 40km) → Ankadikely en premier

## Architecture

### Nouveaux Composants

#### 1. ReservationGroup (Model)
`src/main/java/com/backoffice/model/ReservationGroup.java`

Représente un groupe de réservations partageant le même véhicule:
- Liste des réservations groupées
- Véhicule assigné
- Itinéraire complet avec distances
- Heures de départ et retour

#### 2. GroupingService (Service)
`src/main/java/com/backoffice/service/GroupingService.java`

Service principal qui implémente la logique de regroupement:
- Groupement par fenêtre temporelle
- Algorithme d'optimisation de capacité
- Calcul d'itinéraire optimal
- Assignation de véhicules

#### 3. DistanceValidator (Utility)
`src/main/java/com/backoffice/util/DistanceValidator.java`

Utilitaire pour vérifier que toutes les distances nécessaires existent:
- Détection des distances manquantes
- Génération de rapport
- Création automatique des distances manquantes

#### 4. Controllers
- `PlanificationController`: Méthode `showPlanificationWithGrouping()` ajoutée
- `DistanceValidatorController`: Nouveau contrôleur pour validation des distances

## Utilisation

### 1. Accéder à la planification avec regroupement

```
GET /planification-grouped?dateDebut=2026-03-04&dateFin=2026-03-11
```

Cette méthode:
- Récupère toutes les réservations dans la période
- Groupe les réservations selon les règles
- Assigne les véhicules optimaux
- Calcule les itinéraires complets

### 2. Valider les distances

```
GET /distance-validator
```

Cette méthode affiche un rapport sur les distances manquantes et génère un script SQL pour les ajouter.

### 3. Comparaison des modes

#### Mode Sans Regroupement (`/planification`)
- Une réservation = Un véhicule
- Trajet simple: Aéroport → Hôtel → Aéroport

#### Mode Avec Regroupement (`/planification-grouped`)
- Plusieurs réservations = Un véhicule (si possible)
- Trajet optimisé: Aéroport → Hôtel1 → Hôtel2 → ... → Aéroport
- Économie de véhicules et de carburant

## Algorithme de Regroupement

### Étape 1: Groupement Temporel
Les réservations sont groupées par fenêtre de 30 minutes basée sur l'heure d'arrivée.

### Étape 2: Formation de Groupes
Pour chaque fenêtre temporelle:
1. Trier les réservations par nombre de passagers (décroissant)
2. Pour chaque réservation non assignée:
   - Créer un nouveau groupe
   - Essayer d'ajouter d'autres réservations compatibles
   - Vérifier: total_passagers < capacité_véhicule

### Étape 3: Calcul d'Itinéraire
Pour chaque groupe:
1. Récupérer les destinations (hôtels)
2. Calculer la distance aéroport-hôtel pour chaque destination
3. Trier par distance (puis alphabétique si égal)
4. Construire l'itinéraire: Aéroport → H1 → H2 → ... → Aéroport
5. Calculer la distance totale et les temps

### Étape 4: Assignation de Véhicule
1. Trouver les véhicules candidats (capacité >= total passagers)
2. Trier par: places proches du besoin, puis Diesel > Essence > Hybride > Électrique
3. Vérifier la disponibilité (pas de chevauchement d'occupation)
4. Assigner le premier véhicule disponible

## Exemple Concret

### Données d'Entrée
```
Réservation R1: 4 passagers, Ivato → Colbert (60km), 10:00
Réservation R2: 2 passagers, Ivato → Ibis (40km), 10:00
Véhicule V1: 7 places, Diesel
```

### Résultat
```
Groupe G1:
- Réservations: R1 + R2 (6 passagers total)
- Véhicule: V1 (7 places, Diesel)
- Itinéraire: 
  1. Ivato → Ibis (40km)
  2. Ibis → Colbert (20km)
  3. Colbert → Ivato (60km)
- Distance totale: 120km
- Heure départ: 10:00 + temps d'attente
- Heure retour: départ + (120km / vitesse moyenne)
```

## Prérequis Important

### Base de Données - Distances

**Toutes les distances entre les lieux doivent exister dans la table `distance`.**

Pour un système avec N lieux, il faut N×(N-1)/2 entrées de distances.

Exemple avec 4 lieux (Ivato, Ibis, Colbert, Ankadikely):
```sql
-- 6 combinaisons nécessaires (4×3/2 = 6)
INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES (1, 2, 40.0);  -- Ivato-Ibis
INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES (1, 3, 60.0);  -- Ivato-Colbert
INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES (1, 4, 35.0);  -- Ivato-Ankadikely
INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES (2, 3, 20.0);  -- Ibis-Colbert
INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES (2, 4, 15.0);  -- Ibis-Ankadikely
INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES (3, 4, 25.0);  -- Colbert-Ankadikely
```

**Utiliser `/distance-validator` pour détecter les distances manquantes.**

## Avantages du Système

1. **Optimisation des ressources**: Moins de véhicules nécessaires
2. **Économie de coûts**: Réduction de carburant et d'usure
3. **Respect des règles métier**: Implémentation stricte des contraintes
4. **Flexibilité**: Facile d'ajouter de nouvelles règles
5. **Traçabilité**: Itinéraire complet pour chaque groupe

## Améliorations Futures

- [ ] Interface utilisateur pour visualiser les groupes
- [ ] Modification manuelle des groupes par l'administrateur
- [ ] Optimisation avancée (algorithme génétique, travelling salesman)
- [ ] Export des itinéraires en PDF
- [ ] Notification des chauffeurs avec itinéraire détaillé
