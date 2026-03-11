package com.backoffice.service;

import com.backoffice.dao.AeroportDAO;
import com.backoffice.dao.DistanceDAO;
import com.backoffice.dao.HotelDAO;
import com.backoffice.dao.VehiculeDAO;
import com.backoffice.model.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de regroupement de réservations selon les règles métier:
 * 1. Les réservations avec même date/heure d'arrivée peuvent être groupées
 * 2. Total passagers doit être < capacité du véhicule (pas égal)
 * 3. Ordre de visite: distance la plus courte d'abord
 * 4. Si distances égales: ordre alphabétique
 */
public class GroupingService {
    private final VehiculeDAO vehiculeDAO;
    private final HotelDAO hotelDAO;
    private final AeroportDAO aeroportDAO;
    private final DistanceDAO distanceDAO;
    private int tempsAttenteMinutes = 30; // valeur par défaut, sera mise à jour depuis Parametre

    public GroupingService(VehiculeDAO vehiculeDAO, HotelDAO hotelDAO,
            AeroportDAO aeroportDAO, DistanceDAO distanceDAO) {
        this.vehiculeDAO = vehiculeDAO;
        this.hotelDAO = hotelDAO;
        this.aeroportDAO = aeroportDAO;
        this.distanceDAO = distanceDAO;
    }

    /**
     * Groupe les réservations et assigne les véhicules de manière optimale
     * 
     * @param reservations Liste des réservations à grouper
     * @param parametre    Paramètres pour le calcul des temps
     * @return Liste des groupes de réservations avec véhicules assignés
     */
    public List<ReservationGroup> groupReservations(List<Reservation> reservations, Parametre parametre)
            throws SQLException {

        List<ReservationGroup> groups = new ArrayList<>();

        // Stocker le temps d'attente pour l'utiliser dans groupByTimeSlot
        this.tempsAttenteMinutes = parametre.getTempsAttente();

        // Trier les réservations par date d'arrivée
        List<Reservation> sortedReservations = new ArrayList<>(reservations);
        sortedReservations.sort(Comparator.comparing(Reservation::getDateArrivee));

        // Tracker des véhicules occupés
        List<VehiculeOccupation> occupations = new ArrayList<>();

        // Grouper les réservations par fenêtre de temps (même heure d'arrivée = même
        // vol)
        Map<String, List<Reservation>> reservationsByTimeSlot = groupByTimeSlot(sortedReservations);

        System.out.println("=== GROUPEMENT DES RÉSERVATIONS ===");
        System.out.println("Total réservations: " + sortedReservations.size());
        System.out.println("Fenêtres temporelles: " + reservationsByTimeSlot.size());
        for (Map.Entry<String, List<Reservation>> entry : reservationsByTimeSlot.entrySet()) {
            System.out.println("  Fenêtre " + entry.getKey() + ": " + entry.getValue().size() + " réservation(s)");
            for (Reservation r : entry.getValue()) {
                System.out.println("    - " + r.getClientId() + " (" + r.getNombrePassager() + 
                                 " pers) à " + r.getDateArrivee());
            }
        }

        // Pour chaque fenêtre de temps
        for (Map.Entry<String, List<Reservation>> entry : reservationsByTimeSlot.entrySet()) {
            List<Reservation> slotReservations = entry.getValue();

            // Essayer de former des groupes optimaux
            List<ReservationGroup> slotGroups = formGroups(slotReservations, parametre, occupations);
            groups.addAll(slotGroups);
        }

        return groups;
    }

    /**
     * Groupe les réservations par fenêtre de temps d'attente glissante (Sprint 5).
     *
     * Règle :
     * - On prend la première réservation (triée par heure d'arrivée) comme ancre.
     * - On ouvre une fenêtre [heure_arrivée_ancre, heure_arrivée_ancre + temps_attente].
     * - Toutes les réservations dont l'heure d'arrivée tombe dans cette fenêtre sont groupées.
     * - La fenêtre suivante commence à la prochaine réservation APRÈS la fin de la fenêtre.
     *
     * Exemple avec temps_attente = 30 min :
     *   resa 1: 08:00, resa 2: 08:18, resa 3: 08:40, resa 4: 09:00
     *   Fenêtre 1: [08:00, 08:30] → resa 1 + resa 2
     *   Fenêtre 2: [08:40, 09:10] → resa 3 + resa 4
     */
    private Map<String, List<Reservation>> groupByTimeSlot(List<Reservation> reservations) {
        Map<String, List<Reservation>> grouped = new LinkedHashMap<>();

        if (reservations.isEmpty()) {
            return grouped;
        }

        // D'abord, grouper les réservations par jour
        Map<String, List<Reservation>> byDay = new LinkedHashMap<>();
        for (Reservation r : reservations) {
            String dayKey = String.format("%tF", r.getDateArrivee());
            byDay.computeIfAbsent(dayKey, k -> new ArrayList<>()).add(r);
        }

        int windowIndex = 1;

        for (Map.Entry<String, List<Reservation>> dayEntry : byDay.entrySet()) {
            List<Reservation> dayReservations = dayEntry.getValue();
            // Déjà triées par date d'arrivée (le tri global a été fait avant)

            int i = 0;
            while (i < dayReservations.size()) {
                // L'ancre est la première réservation non encore assignée
                Reservation anchor = dayReservations.get(i);
                long anchorTime = anchor.getDateArrivee().getTime();
                long windowEnd = anchorTime + (tempsAttenteMinutes * 60 * 1000L);

                // Clé unique pour cette fenêtre
                String slotKey = String.format("%tF_window_%d", anchor.getDateArrivee(), windowIndex);
                List<Reservation> windowGroup = new ArrayList<>();
                windowGroup.add(anchor);

                int j = i + 1;
                while (j < dayReservations.size()) {
                    Reservation candidate = dayReservations.get(j);
                    long candidateTime = candidate.getDateArrivee().getTime();

                    if (candidateTime <= windowEnd) {
                        // Cette réservation tombe dans la fenêtre → on l'ajoute
                        windowGroup.add(candidate);
                        j++;
                    } else {
                        // Cette réservation est après la fenêtre → nouvelle fenêtre
                        break;
                    }
                }

                grouped.put(slotKey, windowGroup);
                windowIndex++;
                i = j; // Avancer à la prochaine réservation non traitée
            }
        }

        System.out.println("=== FENÊTRES DE TEMPS D'ATTENTE (Sprint 5) ===");
        System.out.println("Temps d'attente: " + tempsAttenteMinutes + " minutes");
        for (Map.Entry<String, List<Reservation>> entry : grouped.entrySet()) {
            List<Reservation> slot = entry.getValue();
            Reservation first = slot.get(0);
            long windowEndMs = first.getDateArrivee().getTime() + (tempsAttenteMinutes * 60 * 1000L);
            Timestamp windowEndTs = new Timestamp(windowEndMs);
            System.out.println("Fenêtre '" + entry.getKey() + "': " +
                    first.getDateArrivee() + " → " + windowEndTs +
                    " (" + slot.size() + " réservation(s))");
            for (Reservation r : slot) {
                System.out.println("  - Résa #" + r.getId() + " arrivée: " + r.getDateArrivee() +
                        " passagers: " + r.getNombrePassager());
            }
        }

        return grouped;
    }

    /**
     * Forme des groupes optimaux pour un ensemble de réservations
     * Règles:
     * - Essayer de combiner jusqu'à ce que la somme < capacité véhicule
     * - Les clients sont indivisibles
     */
    private List<ReservationGroup> formGroups(List<Reservation> reservations,
            Parametre parametre,
            List<VehiculeOccupation> occupations)
            throws SQLException {

        List<ReservationGroup> groups = new ArrayList<>();
        Set<Integer> assigned = new HashSet<>();

        // Trier par nombre de passagers (décroissant) pour optimiser le remplissage
        List<Reservation> sorted = new ArrayList<>(reservations);
        sorted.sort((r1, r2) -> Integer.compare(r2.getNombrePassager(), r1.getNombrePassager()));

        for (int i = 0; i < sorted.size(); i++) {
            if (assigned.contains(sorted.get(i).getId())) {
                continue;
            }

            ReservationGroup group = new ReservationGroup();
            Reservation firstRes = sorted.get(i);
            group.addReservation(firstRes);
            assigned.add(firstRes.getId());
            
            System.out.println("=== Création groupe: Client=" + firstRes.getClientId() + 
                             ", Passagers=" + firstRes.getNombrePassager());

            // Essayer d'ajouter d'autres réservations compatibles
            for (int j = i + 1; j < sorted.size(); j++) {
                if (assigned.contains(sorted.get(j).getId())) {
                    continue;
                }

                Reservation nextRes = sorted.get(j);
                int totalPassagers = group.getTotalPassagers() + nextRes.getNombrePassager();
                
                System.out.println("  Tentative ajout: Client=" + nextRes.getClientId() + 
                                 ", Passagers=" + nextRes.getNombrePassager() + 
                                 ", Total=" + totalPassagers);

                // Vérifier s'il existe un véhicule qui peut accueillir ce total
                // La règle est: total passagers <= capacité (inférieur ou égal)
                List<Vehicule> vehiculesCandidats = vehiculeDAO.findBestVehicles(totalPassagers);
                
                System.out.println("  Véhicules candidats: " + vehiculesCandidats.size());

                boolean canGroup = false;
                for (Vehicule v : vehiculesCandidats) {
                    System.out.println("    " + v.getReference() + " (" + v.getNombrePlace() + 
                                     " pl): " + totalPassagers + " <= " + v.getNombrePlace() + 
                                     " ? " + (totalPassagers <= v.getNombrePlace()));
                    if (totalPassagers <= v.getNombrePlace()) {
                        canGroup = true;
                        break;
                    }
                }

                if (canGroup) {
                    System.out.println("  ✓ Ajout au groupe");
                    group.addReservation(nextRes);
                    assigned.add(nextRes.getId());
                } else {
                    System.out.println("  ✗ Pas de véhicule compatible");
                }
            }

            // Assigner un véhicule à ce groupe
            assignVehiculeToGroup(group, parametre, occupations);
            groups.add(group);
        }

        return groups;
    }

    /**
     * Assigne un véhicule à un groupe et calcule l'itinéraire
     */
    private void assignVehiculeToGroup(ReservationGroup group, Parametre parametre,
            List<VehiculeOccupation> occupations)
            throws SQLException {

        int totalPassagers = group.getTotalPassagers();

        // Récupérer la première réservation pour les infos temporelles
        Reservation firstReservation = group.getReservations().get(0);

        // Calculer heure de départ = date arrivée + temps d'attente
        long heureDepartMs = firstReservation.getDateArrivee().getTime() +
                (parametre.getTempsAttente() * 60 * 1000L);
        Timestamp heureDepart = new Timestamp(heureDepartMs);
        group.setHeureDepartAeroport(heureDepart);

        // Calculer l'itinéraire pour ce groupe
        List<DestinationInfo> destinations = buildDestinationList(group);

        // Trier les destinations selon les règles: distance puis alphabétique
        destinations.sort((d1, d2) -> {
            int distCompare = Double.compare(d1.distanceFromAeroport, d2.distanceFromAeroport);
            if (distCompare != 0) {
                return distCompare;
            }
            return d1.hotelNom.compareTo(d2.hotelNom);
        });

        // Construire l'itinéraire complet
        List<ReservationGroup.TrajetEtape> itineraire = new ArrayList<>();
        double distanceTotale = 0;
        String lieuActuel = null;

        // Récupérer l'aéroport
        if (firstReservation.getAeroportId() > 0) {
            Aeroport aeroport = aeroportDAO.findById(firstReservation.getAeroportId());
            if (aeroport != null) {
                lieuActuel = aeroport.getLibelle();
            }
        }

        // Parcourir chaque destination
        for (DestinationInfo dest : destinations) {
            ReservationGroup.TrajetEtape etape = new ReservationGroup.TrajetEtape(
                    lieuActuel,
                    dest.hotelNom,
                    dest.distanceFromPrevious,
                    dest.reservation);
            itineraire.add(etape);
            distanceTotale += dest.distanceFromPrevious;
            lieuActuel = dest.hotelNom;
        }

        // Retour à l'aéroport depuis la dernière destination
        if (!destinations.isEmpty()) {
            DestinationInfo lastDest = destinations.get(destinations.size() - 1);
            double distanceRetour = distanceDAO.getDistanceKm(lastDest.hotelLieuxId,
                    lastDest.aeroportLieuxId);

            ReservationGroup.TrajetEtape retour = new ReservationGroup.TrajetEtape(
                    lieuActuel,
                    firstReservation.getAeroportNom(),
                    distanceRetour,
                    null);
            itineraire.add(retour);
            distanceTotale += distanceRetour;
        }

        group.setItineraire(itineraire);
        group.setDistanceTotaleKm(distanceTotale);

        // Calculer le temps total de trajet
        int tempsTrajetMinutes = parametre.calculerTempsTrajet(distanceTotale);
        long heureRetourMs = heureDepartMs + (tempsTrajetMinutes * 60 * 1000L);
        Timestamp heureRetour = new Timestamp(heureRetourMs);
        group.setHeureRetourAeroport(heureRetour);

        // Trouver le meilleur véhicule disponible
        List<Vehicule> candidats = vehiculeDAO.findBestVehicles(totalPassagers);
        
        System.out.println("Assignation véhicule pour " + totalPassagers + " passagers:");
        System.out.println("  Véhicules candidats: " + candidats.size());

        Vehicule vehiculeChoisi = null;
        for (Vehicule v : candidats) {
            boolean occupe = isVehiculeOccupe(occupations, v.getId(), heureDepart, heureRetour);
            System.out.println("  " + v.getReference() + " (" + v.getNombrePlace() + 
                             " pl, " + v.getTypeCarburantLibelle() + "): " + 
                             (occupe ? "OCCUPÉ" : "DISPONIBLE"));
            if (!occupe) {
                vehiculeChoisi = v;
                break;
            }
        }

        if (vehiculeChoisi != null) {
            System.out.println("  → Véhicule choisi: " + vehiculeChoisi.getReference());
            group.setVehicule(vehiculeChoisi);
            occupations.add(new VehiculeOccupation(vehiculeChoisi.getId(), heureDepart, heureRetour));

            // Mettre à jour les infos de chaque réservation du groupe
            System.out.println("  Mise à jour de " + group.getReservations().size() + " réservation(s)");
            for (Reservation r : group.getReservations()) {
                r.setVehiculeReference(vehiculeChoisi.getReference());
                r.setVehiculeTypeCarburant(vehiculeChoisi.getTypeCarburantLibelle());
                r.setVehiculeNombrePlace(vehiculeChoisi.getNombrePlace());
                r.setHeureDepartAeroport(heureDepart);
                r.setHeureRetourAeroport(heureRetour);
                r.setDistanceKm(distanceTotale);
                System.out.println("    - Client " + r.getClientId() + " → " + vehiculeChoisi.getReference());
            }
        } else {
            System.out.println("  ✗ Aucun véhicule disponible!");
        }
    }

    /**
     * Construit la liste des destinations avec leurs distances
     */
    private List<DestinationInfo> buildDestinationList(ReservationGroup group) throws SQLException {
        List<DestinationInfo> destinations = new ArrayList<>();

        for (Reservation r : group.getReservations()) {
            Hotel hotel = hotelDAO.findById(r.getHotelId());
            Aeroport aeroport = aeroportDAO.findById(r.getAeroportId());

            if (hotel != null && aeroport != null) {
                double distanceFromAeroport = distanceDAO.getDistanceKm(
                        aeroport.getLieuxId(),
                        hotel.getLieuxId());

                DestinationInfo info = new DestinationInfo();
                info.reservation = r;
                info.hotelNom = hotel.getNom();
                info.hotelLieuxId = hotel.getLieuxId();
                info.aeroportLieuxId = aeroport.getLieuxId();
                info.distanceFromAeroport = distanceFromAeroport;

                destinations.add(info);
            }
        }

        // Calculer les distances entre destinations consécutives
        if (destinations.size() > 1) {
            for (int i = 0; i < destinations.size(); i++) {
                if (i == 0) {
                    destinations.get(i).distanceFromPrevious = destinations.get(i).distanceFromAeroport;
                } else {
                    double dist = distanceDAO.getDistanceKm(
                            destinations.get(i - 1).hotelLieuxId,
                            destinations.get(i).hotelLieuxId);
                    destinations.get(i).distanceFromPrevious = dist;
                }
            }
        } else if (destinations.size() == 1) {
            destinations.get(0).distanceFromPrevious = destinations.get(0).distanceFromAeroport;
        }

        return destinations;
    }

    /**
     * Vérifie si un véhicule est occupé pendant une période
     */
    private boolean isVehiculeOccupe(List<VehiculeOccupation> occupations, int vehiculeId,
            Timestamp debut, Timestamp fin) {
        for (VehiculeOccupation occ : occupations) {
            if (occ.vehiculeId == vehiculeId) {
                if (debut.before(occ.fin) && fin.after(occ.debut)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Classe interne pour tracker l'occupation d'un véhicule
     */
    private static class VehiculeOccupation {
        int vehiculeId;
        Timestamp debut;
        Timestamp fin;

        VehiculeOccupation(int vehiculeId, Timestamp debut, Timestamp fin) {
            this.vehiculeId = vehiculeId;
            this.debut = debut;
            this.fin = fin;
        }
    }

    /**
     * Classe interne pour gérer les informations de destination
     */
    private static class DestinationInfo {
        Reservation reservation;
        String hotelNom;
        int hotelLieuxId;
        int aeroportLieuxId;
        double distanceFromAeroport;
        double distanceFromPrevious;
    }
}
