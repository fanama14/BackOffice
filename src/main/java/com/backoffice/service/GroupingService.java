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
 * ASSIGNATION PAR PROXIMITÉ AUX PLACES RESTANTES DU VÉHICULE.
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
        for (Reservation r : sorted) {
            if (r.getNombrePassagerOrigine() <= 0) {
                r.setNombrePassagerOrigine(r.getNombrePassager());
            }
        }

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
            long previousWindowEndMs = Long.MIN_VALUE;

            while (i < dayReservations.size() || !unassigned.isEmpty()) {
                long anchorTime;
                long currentWindowEndMs;
                WindowAnchorType windowAnchorType;
                List<Reservation> windowNew = new ArrayList<>();
                int j = i;

                if (!unassigned.isEmpty()) {
                    // Tant qu'il existe des reliquats, on ancre d'abord la fenêtre
                    // sur le prochain instant utile. Les nouvelles réservations ne sont
                    // intégrées que si elles tombent dans cette même fenêtre de reprise.
                    Long nextReservationTime = i < dayReservations.size()
                            ? dayReservations.get(i).getDateArrivee().getTime()
                            : null;
                        RetryAnchor retryAnchor = computeRetryAnchor(
                            unassigned,
                            occupations,
                            allVehicules,
                            nextReservationTime,
                            previousWindowEndMs);
                    anchorTime = retryAnchor.timeMs;
                    windowAnchorType = retryAnchor.type;
                    currentWindowEndMs = anchorTime + (tempsAttenteMin * 60 * 1000L);
                    j = i;
                    while (j < dayReservations.size()) {
                        if (dayReservations.get(j).getDateArrivee().getTime() <= currentWindowEndMs) {
                            windowNew.add(dayReservations.get(j));
                            j++;
                        } else {
                            break;
                        }
                    }

                    // Évite la boucle infinie: si aucune nouvelle réservation n'entre
                    // dans la fenêtre de reprise, on force l'ancrage sur la prochaine
                    // réservation pour faire progresser l'index i.
                    if (windowNew.isEmpty() && i < dayReservations.size()) {
                        Reservation nextAnchor = dayReservations.get(i);
                        anchorTime = nextAnchor.getDateArrivee().getTime();
                        windowAnchorType = WindowAnchorType.RESERVATION;
                        currentWindowEndMs = anchorTime + (tempsAttenteMin * 60 * 1000L);
                        windowNew.add(nextAnchor);
                        j = i + 1;
                        while (j < dayReservations.size()) {
                            if (dayReservations.get(j).getDateArrivee().getTime() <= currentWindowEndMs) {
                                windowNew.add(dayReservations.get(j));
                                j++;
                            } else {
                                break;
                            }
                        }
                    }
                } else if (i < dayReservations.size()) {
                    // Ancre = première réservation non traitée
                    Reservation anchor = dayReservations.get(i);
                    anchorTime = anchor.getDateArrivee().getTime();
                    windowAnchorType = WindowAnchorType.RESERVATION;
                    currentWindowEndMs = anchorTime + (tempsAttenteMin * 60 * 1000L);

                    // Collecter les nouvelles réservations dans la fenêtre
                    windowNew.add(anchor);
                    j = i + 1;
                    while (j < dayReservations.size()) {
                        if (dayReservations.get(j).getDateArrivee().getTime() <= currentWindowEndMs) {
                            windowNew.add(dayReservations.get(j));
                            j++;
                        } else {
                            break;
                        }
                    }
                } else {
                    break;
                }

                // Tous les clients : non-assignés reportés + nouveaux
                List<Reservation> windowClients = new ArrayList<>();
                windowClients.addAll(unassigned);
                windowClients.addAll(windowNew);
                previousWindowEndMs = currentWindowEndMs;

                long latestWindowReservationArrivalMs = anchorTime;
                for (Reservation reservation : windowNew) {
                    latestWindowReservationArrivalMs = Math.max(
                            latestWindowReservationArrivalMs,
                            reservation.getDateArrivee().getTime());
                }

                // Ordre de base: nb passagers décroissant puis arrivée.
                windowClients.sort((a, b) -> {
                    if (a.isPrioriteAssignation() != b.isPrioriteAssignation()) {
                        return a.isPrioriteAssignation() ? -1 : 1;
                    }
                    int byOrigin = Integer.compare(priorityPassengerCount(b), priorityPassengerCount(a));
                    if (byOrigin != 0) {
                        return byOrigin;
                    }
                    int byPassengers = Integer.compare(b.getNombrePassager(), a.getNombrePassager());
                    if (byPassengers != 0) {
                        return byPassengers;
                    }
                    return a.getDateArrivee().compareTo(b.getDateArrivee());
                });

                // Charger les infos hôtel / aéroport / distance
                for (Reservation r : windowClients) {
                    loadReservationInfo(r);
                }

                // État des véhicules pour cette fenêtre
                Map<Integer, VehicleWindowState> vehicleStates = new LinkedHashMap<>();
                List<Reservation> nextUnassigned = new ArrayList<>();
                long latestAssignedArrival = anchorTime;
                long dispatchTimeMs = anchorTime;

                // === ASSIGNATION PAR MEILLEURE PROXIMITÉ (split possible) ===
                List<Reservation> pendingClients = new ArrayList<>(windowClients);

                while (!pendingClients.isEmpty()) {
                    // Règle stricte: traiter d'abord la réservation avec le plus grand
                    // nombre de passagers (puis arrivée la plus tôt).
                    Reservation seedClient = selectHighestPriorityClient(pendingClients);
                    if (seedClient == null) {
                        break;
                    }

                    // Essayer d'abord de placer entièrement ce client dans un véhicule
                    // déjà ouvert dans la fenêtre.
                    VehicleWindowState openedFit = findBestOpenedVehicleForClient(
                            vehicleStates,
                            seedClient.getNombrePassager());
                    if (openedFit != null) {
                        pendingClients.remove(seedClient);
                        openedFit.remainingSeats -= seedClient.getNombrePassager();
                        openedFit.clients.add(splitReservation(seedClient, seedClient.getNombrePassager()));
                        openedFit.latestAssignedArrivalMs = Math.max(
                                openedFit.latestAssignedArrivalMs,
                                seedClient.getDateArrivee().getTime());
                        long openedLatestArrival = fillRemainingSeatsInOpenedVehicle(openedFit, pendingClients);
                        openedFit.latestAssignedArrivalMs = Math.max(openedFit.latestAssignedArrivalMs, openedLatestArrival);
                        latestAssignedArrival = Math.max(
                                latestAssignedArrival,
                                openedFit.latestAssignedArrivalMs);
                        continue;
                    }

                    // RÈGLES 3-5 : trouver un nouveau véhicule
                    List<Vehicule> candidates = new ArrayList<>();
                    for (Vehicule v : allVehicules) {
                        if (v.getNombrePlace() <= 0)
                            continue;
                        if (vehicleStates.containsKey(v.getId()))
                            continue;
                        if (!isAvailableAtInitialHour(v, dispatchTimeMs))
                            continue;
                        if (isOccupied(occupations, v.getId(), dispatchTimeMs))
                            continue;
                        candidates.add(v);
                    }

                    boolean hasFitNow = false;
                    for (Vehicule candidateVehicule : candidates) {
                        if (candidateVehicule.getNombrePlace() >= seedClient.getNombrePassager()) {
                            hasFitNow = true;
                            break;
                        }
                    }

                    if (!hasFitNow) {
                        Long nextFitAvailability = findNextFittingVehicleAvailabilityTime(
                                allVehicules,
                                vehicleStates,
                                occupations,
                                dispatchTimeMs,
                                seedClient.getNombrePassager());
                        if (nextFitAvailability != null
                                && nextFitAvailability > dispatchTimeMs
                                && nextFitAvailability <= currentWindowEndMs) {
                            dispatchTimeMs = nextFitAvailability;
                            continue;
                        }
                    }

                    Vehicule chosen = null;
                    for (Vehicule candidateVehicule : candidates) {
                        if (chosen == null || compareVehicleClientPair(
                                candidateVehicule,
                                seedClient,
                                chosen,
                                seedClient,
                                occupations,
                                historicalTripCounts) < 0) {
                            chosen = candidateVehicule;
                        }
                    }

                    if (chosen == null) {
                        Long nextAvailability = findNextVehicleAvailabilityTime(
                                allVehicules,
                                vehicleStates,
                                occupations,
                                dispatchTimeMs,
                                seedClient.getNombrePassager());
                        if (nextAvailability != null
                                && nextAvailability > dispatchTimeMs
                                && nextAvailability <= currentWindowEndMs) {
                            dispatchTimeMs = nextAvailability;
                            continue;
                        }
                        break;
                    }

                    pendingClients.remove(seedClient);
                    int allocated = Math.min(seedClient.getNombrePassager(), chosen.getNombrePlace());

                    VehicleWindowState state = new VehicleWindowState();
                    state.vehicule = chosen;
                    state.remainingSeats = chosen.getNombrePlace() - allocated;
                    state.dispatchTimeMs = dispatchTimeMs;
                    state.latestAssignedArrivalMs = seedClient.getDateArrivee().getTime();
                    state.clients = new ArrayList<>();
                    state.clients.add(splitReservation(seedClient, allocated));
                    vehicleStates.put(chosen.getId(), state);

                    latestAssignedArrival = Math.max(latestAssignedArrival, seedClient.getDateArrivee().getTime());

                    int remainderPassengers = seedClient.getNombrePassager() - allocated;
                    if (remainderPassengers > 0) {
                        Reservation remainder = splitReservation(seedClient, remainderPassengers);
                        // Un reliquat dans la même fenêtre est un "reste", pas encore un non assigné reporté.
                        remainder.setPrioriteAssignation(false);
                        pendingClients.add(remainder);
                    }

                    long stateLatestArrival = fillRemainingSeatsInOpenedVehicle(state, pendingClients);
                    state.latestAssignedArrivalMs = Math.max(state.latestAssignedArrivalMs, stateLatestArrival);
                    latestAssignedArrival = Math.max(latestAssignedArrival, state.latestAssignedArrivalMs);

                }

                nextUnassigned.addAll(pendingClients);

                // Créer les groupes pour chaque véhicule assigné
                for (VehicleWindowState state : vehicleStates.values()) {
                    long baseDepartureMs = Math.max(state.latestAssignedArrivalMs, state.dispatchTimeMs);
                    long taDepartureFloorMs = Math.max(baseDepartureMs, latestWindowReservationArrivalMs);
                    boolean canDepartDirectly = canDepartImmediatelyFromAvailability(state);
                    long departureMs = canDepartDirectly ? baseDepartureMs : taDepartureFloorMs;
                    Timestamp departureTime = new Timestamp(departureMs);
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

                for (Reservation carry : nextUnassigned) {
                    carry.setPrioriteAssignation(true);
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
            if (occ.vehiculeId == vehiculeId
                    && occ.debut.getTime() <= time
                    && time < occ.fin.getTime()) {
                return true;
            }
        }
        return false;
    }

    private Long findNextVehicleAvailabilityTime(
            List<Vehicule> allVehicules,
            Map<Integer, VehicleWindowState> vehicleStates,
            List<VehiculeOccupation> occupations,
            long fromTime,
            int requiredPassengers) {

        Long bestFitTime = null;
        Long bestAnyTime = null;

        for (Vehicule v : allVehicules) {
            if (v.getNombrePlace() <= 0)
                continue;
            if (vehicleStates.containsKey(v.getId()))
                continue;

            long next = Math.max(fromTime, computeInitialAvailabilityTime(v, fromTime));
            next = getBusyUntil(occupations, v.getId(), next);

            if (v.getNombrePlace() >= requiredPassengers) {
                if (bestFitTime == null || next < bestFitTime) {
                    bestFitTime = next;
                }
            } else {
                if (bestAnyTime == null || next < bestAnyTime) {
                    bestAnyTime = next;
                }
            }
        }

        return bestFitTime != null ? bestFitTime : bestAnyTime;
    }

    private Long findNextFittingVehicleAvailabilityTime(
            List<Vehicule> allVehicules,
            Map<Integer, VehicleWindowState> vehicleStates,
            List<VehiculeOccupation> occupations,
            long fromTime,
            int requiredPassengers) {

        Long bestFitTime = null;

        for (Vehicule v : allVehicules) {
            if (v.getNombrePlace() <= 0)
                continue;
            if (v.getNombrePlace() < requiredPassengers)
                continue;
            if (vehicleStates.containsKey(v.getId()))
                continue;

            long next = Math.max(fromTime, computeInitialAvailabilityTime(v, fromTime));
            next = getBusyUntil(occupations, v.getId(), next);

            if (bestFitTime == null || next < bestFitTime) {
                bestFitTime = next;
            }
        }

        return bestFitTime;
    }

    private long computeInitialAvailabilityTime(Vehicule vehicule, long referenceTimeMs) {
        if (vehicule == null || vehicule.getHeureDisponibilite() == null) {
            return referenceTimeMs;
        }

        Calendar reference = Calendar.getInstance();
        reference.setTimeInMillis(referenceTimeMs);

        Calendar dispo = Calendar.getInstance();
        dispo.setTimeInMillis(vehicule.getHeureDisponibilite().getTime());

        reference.set(Calendar.HOUR_OF_DAY, dispo.get(Calendar.HOUR_OF_DAY));
        reference.set(Calendar.MINUTE, dispo.get(Calendar.MINUTE));
        reference.set(Calendar.SECOND, dispo.get(Calendar.SECOND));
        reference.set(Calendar.MILLISECOND, 0);

        return reference.getTimeInMillis();
    }

    private long getBusyUntil(List<VehiculeOccupation> occupations, int vehiculeId, long time) {
        long busyUntil = time;
        for (VehiculeOccupation occ : occupations) {
            if (occ.vehiculeId == vehiculeId
                    && occ.debut.getTime() <= time
                    && time < occ.fin.getTime()) {
                busyUntil = Math.max(busyUntil, occ.fin.getTime());
            }
        }
        return busyUntil;
    }

    private boolean hasVehicleWithCapacity(List<Vehicule> vehicules, int requiredPassengers) {
        for (Vehicule v : vehicules) {
            if (v.getNombrePlace() >= requiredPassengers) {
                return true;
            }
        }
        return false;
    }

    private boolean isAvailableAtInitialHour(Vehicule vehicule, long reservationTimeMs) {
        if (vehicule == null || vehicule.getHeureDisponibilite() == null) {
            return true;
        }

        Calendar reservationCalendar = Calendar.getInstance();
        reservationCalendar.setTimeInMillis(reservationTimeMs);

        Calendar heureDispo = Calendar.getInstance();
        heureDispo.setTimeInMillis(vehicule.getHeureDisponibilite().getTime());

        reservationCalendar.set(Calendar.HOUR_OF_DAY, heureDispo.get(Calendar.HOUR_OF_DAY));
        reservationCalendar.set(Calendar.MINUTE, heureDispo.get(Calendar.MINUTE));
        reservationCalendar.set(Calendar.SECOND, heureDispo.get(Calendar.SECOND));
        reservationCalendar.set(Calendar.MILLISECOND, 0);

        return reservationTimeMs >= reservationCalendar.getTimeInMillis();
    }

    private int countTrips(List<VehiculeOccupation> occupations, int vehiculeId) {
        int count = 0;
        for (VehiculeOccupation occ : occupations) {
            if (occ.vehiculeId == vehiculeId)
                count++;
        }
        return count;
    }

    private Reservation selectHighestPriorityClient(List<Reservation> clients) {
        Reservation best = null;
        for (Reservation r : clients) {
            if (best == null) {
                best = r;
                continue;
            }

            if (r.isPrioriteAssignation() != best.isPrioriteAssignation()) {
                if (r.isPrioriteAssignation()) {
                    best = r;
                }
                continue;
            }

            int byOrigin = Integer.compare(priorityPassengerCount(r), priorityPassengerCount(best));
            if (byOrigin > 0) {
                best = r;
                continue;
            }
            if (byOrigin < 0) {
                continue;
            }

            int byPassengers = Integer.compare(r.getNombrePassager(), best.getNombrePassager());
            if (byPassengers > 0) {
                best = r;
                continue;
            }

            if (byPassengers == 0
                    && r.getDateArrivee().before(best.getDateArrivee())) {
                best = r;
            }
        }
        return best;
    }

    private VehicleWindowState findBestOpenedVehicleForClient(
            Map<Integer, VehicleWindowState> vehicleStates,
            int requiredPassengers) {

        VehicleWindowState best = null;
        for (VehicleWindowState state : vehicleStates.values()) {
            if (state.remainingSeats < requiredPassengers) {
                continue;
            }
            if (best == null || state.remainingSeats < best.remainingSeats) {
                best = state;
            }
        }
        return best;
    }

    private Reservation selectBestClientForRemainingSeats(
            List<Reservation> candidates,
            int remainingSeats,
            boolean allowOversize) {
        Reservation best = null;
        for (Reservation candidate : candidates) {
            if (!allowOversize && candidate.getNombrePassager() > remainingSeats) {
                continue;
            }
            if (best == null || compareClientsForRemainingSeats(candidate, best, remainingSeats) < 0) {
                best = candidate;
            }
        }
        return best;
    }

    private int compareClientsForRemainingSeats(Reservation a, Reservation b, int remainingSeats) {
        boolean aFits = a.getNombrePassager() <= remainingSeats;
        boolean bFits = b.getNombrePassager() <= remainingSeats;
        if (aFits != bFits) {
            return aFits ? -1 : 1;
        }

        int diffA = Math.abs(remainingSeats - a.getNombrePassager());
        int diffB = Math.abs(remainingSeats - b.getNombrePassager());
        if (diffA != diffB) {
            return Integer.compare(diffA, diffB);
        }

        int byPassengers = Integer.compare(b.getNombrePassager(), a.getNombrePassager());
        if (byPassengers != 0) {
            return byPassengers;
        }

        return a.getDateArrivee().compareTo(b.getDateArrivee());
    }

    private long fillRemainingSeatsInOpenedVehicle(VehicleWindowState state, List<Reservation> pendingClients) {
        long latestArrival = state.latestAssignedArrivalMs;

        while (state.remainingSeats > 0 && !pendingClients.isEmpty()) {
            Reservation candidate;
            if (hasPrioritizedPending(pendingClients)) {
                candidate = selectHighestPriorityPrioritizedClient(pendingClients);
            } else {
                candidate = selectBestClientForRemainingSeats(pendingClients, state.remainingSeats, true);
            }
            if (candidate == null) {
                break;
            }

            pendingClients.remove(candidate);
            int allocated = Math.min(candidate.getNombrePassager(), state.remainingSeats);
            state.remainingSeats -= allocated;
            state.clients.add(splitReservation(candidate, allocated));
            latestArrival = Math.max(latestArrival, candidate.getDateArrivee().getTime());

            int remainderPassengers = candidate.getNombrePassager() - allocated;
            if (remainderPassengers > 0) {
                Reservation remainder = splitReservation(candidate, remainderPassengers);
                // Tant que la fenêtre courante n'est pas close, on garde ce reliquat comme "reste".
                remainder.setPrioriteAssignation(false);
                pendingClients.add(remainder);
            }
        }

        return latestArrival;
    }

    private boolean containsPrioritizedPassengers(List<Reservation> reservations) {
        if (reservations == null) {
            return false;
        }
        for (Reservation reservation : reservations) {
            if (reservation.isPrioriteAssignation()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPrioritizedPending(List<Reservation> reservations) {
        if (reservations == null) {
            return false;
        }
        for (Reservation reservation : reservations) {
            if (reservation.isPrioriteAssignation()) {
                return true;
            }
        }
        return false;
    }

    private Reservation selectHighestPriorityPrioritizedClient(List<Reservation> clients) {
        Reservation best = null;
        for (Reservation reservation : clients) {
            if (!reservation.isPrioriteAssignation()) {
                continue;
            }

            if (best == null) {
                best = reservation;
                continue;
            }

            int byOrigin = Integer.compare(priorityPassengerCount(reservation), priorityPassengerCount(best));
            if (byOrigin > 0) {
                best = reservation;
                continue;
            }
            if (byOrigin < 0) {
                continue;
            }

            int byPassengers = Integer.compare(reservation.getNombrePassager(), best.getNombrePassager());
            if (byPassengers > 0) {
                best = reservation;
                continue;
            }

            if (byPassengers == 0 && reservation.getDateArrivee().before(best.getDateArrivee())) {
                best = reservation;
            }
        }
        return best;
    }

    private boolean canDepartImmediatelyFromAvailability(VehicleWindowState state) {
        if (state == null || state.remainingSeats != 0) {
            return false;
        }

        if (!containsPrioritizedPassengers(state.clients)) {
            return false;
        }

        for (Reservation reservation : state.clients) {
            if (reservation.isPrioriteAssignation()) {
                continue;
            }
            if (reservation.getDateArrivee() == null
                    || reservation.getDateArrivee().getTime() != state.dispatchTimeMs) {
                return false;
            }
        }

        return true;
    }

    private int compareClientsForVehicleStart(Reservation a, Reservation b) {
        int byPassengers = Integer.compare(b.getNombrePassager(), a.getNombrePassager());
        if (byPassengers != 0) {
            return byPassengers;
        }

        return a.getDateArrivee().compareTo(b.getDateArrivee());
    }

    private int compareVehicleClientPair(
            Vehicule vehiculeA,
            Reservation clientA,
            Vehicule vehiculeB,
            Reservation clientB,
            List<VehiculeOccupation> occupations,
            Map<Integer, Integer> historicalTripCounts) {

        // Contrainte dure: privilégier un véhicule qui peut absorber
        // toute la réservation (pas de split) si possible.
        boolean aFits = vehiculeA.getNombrePlace() >= clientA.getNombrePassager();
        boolean bFits = vehiculeB.getNombrePlace() >= clientB.getNombrePassager();
        if (aFits != bFits) {
            return aFits ? -1 : 1;
        }

        // Priorité métier: le plus grand groupe passe d'abord.
        int byPassengers = Integer.compare(clientB.getNombrePassager(), clientA.getNombrePassager());
        if (byPassengers != 0) {
            return byPassengers;
        }

        if (aFits && bFits) {
            // Si les deux conviennent, minimiser le surplus.
            int surplusA = vehiculeA.getNombrePlace() - clientA.getNombrePassager();
            int surplusB = vehiculeB.getNombrePlace() - clientB.getNombrePassager();
            if (surplusA != surplusB) {
                return Integer.compare(surplusA, surplusB);
            }
        } else {
            // Sinon (aucun ne convient), prendre la capacité la plus proche.
            int diffA = Math.abs(vehiculeA.getNombrePlace() - clientA.getNombrePassager());
            int diffB = Math.abs(vehiculeB.getNombrePlace() - clientB.getNombrePassager());
            if (diffA != diffB) {
                return Integer.compare(diffA, diffB);
            }
        }

        int tripsA = historicalTripCounts.getOrDefault(vehiculeA.getId(), 0)
                + countTrips(occupations, vehiculeA.getId());
        int tripsB = historicalTripCounts.getOrDefault(vehiculeB.getId(), 0)
                + countTrips(occupations, vehiculeB.getId());
        if (tripsA != tripsB) {
            return Integer.compare(tripsA, tripsB);
        }

        int byFuel = Integer.compare(
                fuelPriority(vehiculeA.getTypeCarburant()),
                fuelPriority(vehiculeB.getTypeCarburant()));
        if (byFuel != 0) {
            return byFuel;
        }

        return clientA.getDateArrivee().compareTo(clientB.getDateArrivee());
    }

        private RetryAnchor computeRetryAnchor(
            List<Reservation> unassigned,
            List<VehiculeOccupation> occupations,
            List<Vehicule> allVehicules,
            Long nextReservationTime,
            long previousWindowEndMs) {

        long lowerBound = previousWindowEndMs == Long.MIN_VALUE ? Long.MIN_VALUE : previousWindowEndMs;
        long latestUnassignedArrival = 0;
        for (Reservation r : unassigned) {
            if (r.getDateArrivee() != null) {
                latestUnassignedArrival = Math.max(latestUnassignedArrival, r.getDateArrivee().getTime());
            }
        }

        Long earliestVehicleReturn = null;
        for (VehiculeOccupation occ : occupations) {
            long returnTime = occ.fin.getTime();
            if (returnTime < lowerBound) {
                continue;
            }
            if (earliestVehicleReturn == null || returnTime < earliestVehicleReturn) {
                earliestVehicleReturn = returnTime;
            }
        }

        Long earliestVehicleAvailability = null;
        if (allVehicules != null) {
            for (Vehicule vehicule : allVehicules) {
                if (vehicule == null || vehicule.getId() <= 0) {
                    continue;
                }
                long availableTime = computeInitialAvailabilityTime(vehicule, lowerBound);
                availableTime = getBusyUntil(occupations, vehicule.getId(), availableTime);
                if (availableTime < lowerBound) {
                    continue;
                }
                if (earliestVehicleAvailability == null || availableTime < earliestVehicleAvailability) {
                    earliestVehicleAvailability = availableTime;
                }
            }
        }

        Long candidateReservation = null;
        if (nextReservationTime != null && nextReservationTime >= lowerBound) {
            candidateReservation = nextReservationTime;
        }

        Long candidateVehicle = earliestVehicleAvailability;
        if (earliestVehicleReturn != null
                && (candidateVehicle == null || earliestVehicleReturn < candidateVehicle)) {
            candidateVehicle = earliestVehicleReturn;
        }

        if (candidateVehicle != null
                && (candidateReservation == null || candidateVehicle <= candidateReservation)) {
            long anchor = Math.max(latestUnassignedArrival, candidateVehicle);
            return new RetryAnchor(anchor, WindowAnchorType.VEHICLE_RETURN);
        }

        if (candidateReservation != null) {
            long anchor = Math.max(latestUnassignedArrival, candidateReservation);
            return new RetryAnchor(anchor, WindowAnchorType.RESERVATION);
        }

        long anchor = Math.max(latestUnassignedArrival, lowerBound);
        return new RetryAnchor(anchor, WindowAnchorType.RESERVATION);
    }

    private Reservation splitReservation(Reservation source, int passengerCount) {
        Reservation split = new Reservation();
        split.setId(source.getId());
        split.setClientId(source.getClientId());
        split.setNombrePassager(passengerCount);
        split.setNombrePassagerOrigine(
            source.getNombrePassagerOrigine() > 0 ? source.getNombrePassagerOrigine() : source.getNombrePassager());
        split.setDateArrivee(source.getDateArrivee());
        split.setHotelId(source.getHotelId());
        split.setAeroportId(source.getAeroportId());

        split.setHotelNom(source.getHotelNom());
        split.setAeroportNom(source.getAeroportNom());
        split.setDistanceKm(source.getDistanceKm());
        split.setPrioriteAssignation(source.isPrioriteAssignation());

        return split;
    }

    private int priorityPassengerCount(Reservation reservation) {
        if (reservation == null) {
            return 0;
        }
        return reservation.getNombrePassagerOrigine() > 0
                ? reservation.getNombrePassagerOrigine()
                : reservation.getNombrePassager();
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
        long dispatchTimeMs;
        long latestAssignedArrivalMs;
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

    private enum WindowAnchorType {
        RESERVATION,
        VEHICLE_RETURN
    }

    private static class RetryAnchor {
        long timeMs;
        WindowAnchorType type;

        RetryAnchor(long timeMs, WindowAnchorType type) {
            this.timeMs = timeMs;
            this.type = type;
        }
    }
}