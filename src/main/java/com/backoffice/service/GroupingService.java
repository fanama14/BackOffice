package com.backoffice.service;

import com.backoffice.dao.AeroportDAO;
import com.backoffice.dao.DistanceDAO;
import com.backoffice.dao.HotelDAO;
import com.backoffice.dao.PlanificationDAO;
import com.backoffice.dao.VehiculeDAO;
import com.backoffice.model.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Service de planification des véhicules.
 *
 * ASSIGNATION PAR ORDRE DÉCROISSANT DU NB PASSAGERS.
 *
 * Priorité des règles :
 * 1. NB PLACES >= NB PASSAGERS (contrainte dure)
 * 2. REMPLIR VÉHICULE : si un véhicule déjà assigné a assez de places
 * restantes, on y met le client
 * 3. NB PLACES LE PLUS PROCHE DU NB PASSAGERS
 * 4. VÉHICULE AVEC LE MOINS DE TRAJETS
 * 5. CARBURANT : D > ES > H > EL
 *
 * DISPONIBILITÉ :
 * - Fenêtre de temps d'attente glissante
 * - Départ = heure d'arrivée du dernier client assigné dans la fenêtre
 * - Clients non assignés attendent la prochaine réservation pour une nouvelle
 * fenêtre
 */
public class GroupingService {
    private final VehiculeDAO vehiculeDAO;
    private final HotelDAO hotelDAO;
    private final AeroportDAO aeroportDAO;
    private final DistanceDAO distanceDAO;
    private final PlanificationDAO planificationDAO;

    public GroupingService(VehiculeDAO vehiculeDAO, HotelDAO hotelDAO,
            AeroportDAO aeroportDAO, DistanceDAO distanceDAO, PlanificationDAO planificationDAO) {
        this.vehiculeDAO = vehiculeDAO;
        this.hotelDAO = hotelDAO;
        this.aeroportDAO = aeroportDAO;
        this.distanceDAO = distanceDAO;
        this.planificationDAO = planificationDAO;
    }

    /**
     * Planifie l'assignation des véhicules aux réservations.
     */
    public List<ReservationGroup> planifier(List<Reservation> reservations, Parametre parametre)
            throws SQLException {

        List<ReservationGroup> allGroups = new ArrayList<>();
        if (reservations.isEmpty())
            return allGroups;

        int tempsAttenteMin = parametre.getTempsAttente();
        List<Vehicule> allVehicules = vehiculeDAO.findAll();
        List<VehiculeOccupation> occupations = new ArrayList<>();
        Map<Integer, Integer> historicalTripCounts = planificationDAO.countTripsByVehicule();

        // Trier par date d'arrivée
        List<Reservation> sorted = new ArrayList<>(reservations);
        sorted.sort(Comparator.comparing(Reservation::getDateArrivee));

        // Grouper par jour
        Map<String, List<Reservation>> byDay = new LinkedHashMap<>();
        for (Reservation r : sorted) {
            String dayKey = String.format("%tF", r.getDateArrivee());
            byDay.computeIfAbsent(dayKey, k -> new ArrayList<>()).add(r);
        }

        int groupeIdCounter = 1;

        for (List<Reservation> dayReservations : byDay.values()) {
            List<Reservation> unassigned = new ArrayList<>();
            int i = 0;

            while (i < dayReservations.size() || !unassigned.isEmpty()) {
                long anchorTime;
                List<Reservation> windowNew = new ArrayList<>();
                int j = i;

                if (i < dayReservations.size()) {
                    // Ancre = première réservation non traitée
                    Reservation anchor = dayReservations.get(i);
                    anchorTime = anchor.getDateArrivee().getTime();
                    long windowEndMs = anchorTime + (tempsAttenteMin * 60 * 1000L);

                    // Collecter les nouvelles réservations dans la fenêtre
                    windowNew.add(anchor);
                    j = i + 1;
                    while (j < dayReservations.size()) {
                        if (dayReservations.get(j).getDateArrivee().getTime() <= windowEndMs) {
                            windowNew.add(dayReservations.get(j));
                            j++;
                        } else {
                            break;
                        }
                    }
                } else {
                    // Plus de nouvelles réservations : retenter avec une ancre qui tient compte
                    // des retours de véhicules pour ne pas abandonner trop tôt les non-assignés.
                    anchorTime = computeRetryAnchorTime(unassigned, occupations);
                }

                // Tous les clients : non-assignés reportés + nouveaux
                List<Reservation> windowClients = new ArrayList<>();
                windowClients.addAll(unassigned);
                windowClients.addAll(windowNew);

                // Trier par nb passagers décroissant
                windowClients.sort((a, b) -> Integer.compare(b.getNombrePassager(), a.getNombrePassager()));

                // Charger les infos hôtel / aéroport / distance
                for (Reservation r : windowClients) {
                    loadReservationInfo(r);
                }

                // État des véhicules pour cette fenêtre
                Map<Integer, VehicleWindowState> vehicleStates = new LinkedHashMap<>();
                List<Reservation> nextUnassigned = new ArrayList<>();
                long latestAssignedArrival = anchorTime;

                // === ASSIGNATION CLIENT PAR CLIENT ===
                for (Reservation client : windowClients) {
                    int nbPassagers = client.getNombrePassager();
                    boolean assigned = false;

                    // RÈGLE 2 : REMPLIR – chercher un véhicule déjà assigné avec assez de places
                    for (VehicleWindowState state : vehicleStates.values()) {
                        if (state.remainingSeats >= nbPassagers) {
                            state.remainingSeats -= nbPassagers;
                            state.clients.add(client);
                            assigned = true;
                            break;
                        }
                    }

                    if (!assigned) {
                        // RÈGLES 3-5 : trouver un nouveau véhicule
                        List<Vehicule> candidates = new ArrayList<>();
                        for (Vehicule v : allVehicules) {
                            if (v.getNombrePlace() < nbPassagers)
                                continue;
                            if (vehicleStates.containsKey(v.getId()))
                                continue;
                            if (isOccupied(occupations, v.getId(), anchorTime))
                                continue;
                            candidates.add(v);
                        }

                        if (!candidates.isEmpty()) {
                            final int np = nbPassagers;
                            candidates.sort((a, b) -> {
                                // Règle 3 : capacité la plus proche
                                int diffA = a.getNombrePlace() - np;
                                int diffB = b.getNombrePlace() - np;
                                if (diffA != diffB)
                                    return Integer.compare(diffA, diffB);
                                // Règle 4 : moins de trajets
                                int tripsA = historicalTripCounts.getOrDefault(a.getId(), 0)
                                    + countTrips(occupations, a.getId());
                                int tripsB = historicalTripCounts.getOrDefault(b.getId(), 0)
                                    + countTrips(occupations, b.getId());
                                if (tripsA != tripsB)
                                    return Integer.compare(tripsA, tripsB);
                                // Règle 5 : carburant D > ES > H > EL
                                return Integer.compare(fuelPriority(a.getTypeCarburant()),
                                        fuelPriority(b.getTypeCarburant()));
                            });

                            Vehicule chosen = candidates.get(0);
                            VehicleWindowState state = new VehicleWindowState();
                            state.vehicule = chosen;
                            state.remainingSeats = chosen.getNombrePlace() - nbPassagers;
                            state.clients = new ArrayList<>();
                            state.clients.add(client);
                            vehicleStates.put(chosen.getId(), state);
                            assigned = true;
                        }
                    }

                    if (assigned) {
                        if (client.getDateArrivee().getTime() > latestAssignedArrival) {
                            latestAssignedArrival = client.getDateArrivee().getTime();
                        }
                    } else {
                        nextUnassigned.add(client);
                    }
                }

                // Heure de départ = heure d'arrivée du dernier client assigné
                Timestamp departureTime = new Timestamp(latestAssignedArrival);

                // Créer les groupes pour chaque véhicule assigné
                for (VehicleWindowState state : vehicleStates.values()) {
                    ReservationGroup group = new ReservationGroup();
                    group.setVehicule(state.vehicule);
                    group.setHeureDepartAeroport(departureTime);

                    double routeDistance = calculateRouteDistance(state.clients);
                    group.setDistanceTotaleKm(routeDistance);

                    int tempsTrajet = parametre.calculerTempsTrajet(routeDistance);
                    Timestamp retour = new Timestamp(departureTime.getTime() + tempsTrajet * 60 * 1000L);
                    group.setHeureRetourAeroport(retour);

                    int ordre = 1;
                    for (Reservation r : state.clients) {
                        r.setGroupeId(groupeIdCounter);
                        r.setOrdreLivraison(ordre++);
                        r.setVehiculeReference(state.vehicule.getReference());
                        r.setVehiculeTypeCarburant(state.vehicule.getTypeCarburantLibelle());
                        r.setVehiculeNombrePlace(state.vehicule.getNombrePlace());
                        r.setHeureDepartAeroport(departureTime);
                        r.setHeureRetourAeroport(retour);
                        group.addReservation(r);
                    }

                    occupations.add(new VehiculeOccupation(state.vehicule.getId(), departureTime, retour));
                    allGroups.add(group);
                    groupeIdCounter++;
                }

                // Reporter les non-assignés à la prochaine fenêtre
                if (i >= dayReservations.size() && windowNew.isEmpty() && nextUnassigned.size() == windowClients.size()) {
                    ReservationGroup grp = new ReservationGroup();
                    int ordre = 1;
                    for (Reservation r : nextUnassigned) {
                        r.setGroupeId(groupeIdCounter);
                        r.setOrdreLivraison(ordre++);
                        grp.addReservation(r);
                    }
                    allGroups.add(grp);
                    groupeIdCounter++;
                    unassigned = new ArrayList<>();
                    break;
                }

                unassigned = nextUnassigned;
                i = j;
            }
        }

        return allGroups;
    }

    private void loadReservationInfo(Reservation r) throws SQLException {
        if (r.getHotelNom() == null) {
            Hotel hotel = hotelDAO.findById(r.getHotelId());
            if (hotel != null)
                r.setHotelNom(hotel.getNom());
        }
        if (r.getAeroportNom() == null) {
            Aeroport aeroport = aeroportDAO.findById(r.getAeroportId());
            if (aeroport != null)
                r.setAeroportNom(aeroport.getLibelle());
        }
        if (r.getDistanceKm() <= 0) {
            Hotel hotel = hotelDAO.findById(r.getHotelId());
            Aeroport aeroport = aeroportDAO.findById(r.getAeroportId());
            if (hotel != null && aeroport != null) {
                double dist = distanceDAO.getDistanceKm(aeroport.getLieuxId(), hotel.getLieuxId());
                r.setDistanceKm(dist);
            }
        }
    }

    private double calculateRouteDistance(List<Reservation> clients) throws SQLException {
        if (clients.isEmpty())
            return 0;

        Aeroport aeroport = aeroportDAO.findById(clients.get(0).getAeroportId());
        if (aeroport == null)
            return 0;
        int aeroportLieuxId = aeroport.getLieuxId();

        List<Integer> stopLieuxIds = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        for (Reservation r : clients) {
            if (!visited.contains(r.getHotelId())) {
                Hotel hotel = hotelDAO.findById(r.getHotelId());
                if (hotel != null) {
                    stopLieuxIds.add(hotel.getLieuxId());
                    visited.add(r.getHotelId());
                }
            }
        }

        if (stopLieuxIds.isEmpty())
            return 0;

        // Trier par distance depuis l'aéroport (plus proche d'abord)
        final int aLieuxId = aeroportLieuxId;
        stopLieuxIds.sort((a, b) -> {
            try {
                double da = distanceDAO.getDistanceKm(aLieuxId, a);
                double db = distanceDAO.getDistanceKm(aLieuxId, b);
                return Double.compare(da, db);
            } catch (SQLException e) {
                return 0;
            }
        });

        double total = 0;
        int current = aeroportLieuxId;
        for (int stopLieuxId : stopLieuxIds) {
            total += distanceDAO.getDistanceKm(current, stopLieuxId);
            current = stopLieuxId;
        }
        total += distanceDAO.getDistanceKm(current, aeroportLieuxId);

        return total;
    }

    private boolean isOccupied(List<VehiculeOccupation> occupations, int vehiculeId, long time) {
        for (VehiculeOccupation occ : occupations) {
            if (occ.vehiculeId == vehiculeId && time < occ.fin.getTime()) {
                return true;
            }
        }
        return false;
    }

    private int countTrips(List<VehiculeOccupation> occupations, int vehiculeId) {
        int count = 0;
        for (VehiculeOccupation occ : occupations) {
            if (occ.vehiculeId == vehiculeId)
                count++;
        }
        return count;
    }

    private long computeRetryAnchorTime(List<Reservation> unassigned, List<VehiculeOccupation> occupations) {
        long latestUnassignedArrival = 0;
        for (Reservation r : unassigned) {
            if (r.getDateArrivee() != null) {
                latestUnassignedArrival = Math.max(latestUnassignedArrival, r.getDateArrivee().getTime());
            }
        }

        long earliestVehicleReturn = Long.MAX_VALUE;
        for (VehiculeOccupation occ : occupations) {
            earliestVehicleReturn = Math.min(earliestVehicleReturn, occ.fin.getTime());
        }

        if (earliestVehicleReturn == Long.MAX_VALUE) {
            return latestUnassignedArrival;
        }
        return Math.max(latestUnassignedArrival, earliestVehicleReturn);
    }

    private int fuelPriority(String type) {
        if (type == null)
            return 5;
        switch (type) {
            case "D":
                return 1;
            case "ES":
                return 2;
            case "H":
                return 3;
            case "EL":
                return 4;
            default:
                return 5;
        }
    }

    private static class VehicleWindowState {
        Vehicule vehicule;
        int remainingSeats;
        List<Reservation> clients;
    }

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
}
