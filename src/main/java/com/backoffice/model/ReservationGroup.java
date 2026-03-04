package com.backoffice.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un groupe de réservations qui partagent le même véhicule.
 * Les réservations sont groupées selon les règles métier:
 * - Même date/heure d'arrivée
 * - Total de passagers <= capacité du véhicule
 * - Itinéraire optimisé par distance (plus proche en premier, puis
 * alphabétique)
 */
public class ReservationGroup {
    private List<Reservation> reservations;
    private Vehicule vehicule;
    private Timestamp heureDepartAeroport;
    private Timestamp heureRetourAeroport;
    private List<TrajetEtape> itineraire;
    private double distanceTotaleKm;

    public ReservationGroup() {
        this.reservations = new ArrayList<>();
        this.itineraire = new ArrayList<>();
    }

    public void addReservation(Reservation reservation) {
        this.reservations.add(reservation);
    }

    public int getTotalPassagers() {
        return reservations.stream()
                .mapToInt(Reservation::getNombrePassager)
                .sum();
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public Timestamp getHeureDepartAeroport() {
        return heureDepartAeroport;
    }

    public void setHeureDepartAeroport(Timestamp heureDepartAeroport) {
        this.heureDepartAeroport = heureDepartAeroport;
    }

    public Timestamp getHeureRetourAeroport() {
        return heureRetourAeroport;
    }

    public void setHeureRetourAeroport(Timestamp heureRetourAeroport) {
        this.heureRetourAeroport = heureRetourAeroport;
    }

    public List<TrajetEtape> getItineraire() {
        return itineraire;
    }

    public void setItineraire(List<TrajetEtape> itineraire) {
        this.itineraire = itineraire;
    }

    public double getDistanceTotaleKm() {
        return distanceTotaleKm;
    }

    public void setDistanceTotaleKm(double distanceTotaleKm) {
        this.distanceTotaleKm = distanceTotaleKm;
    }

    /**
     * Représente une étape du trajet (un segment du parcours)
     */
    public static class TrajetEtape {
        private String lieuDepart;
        private String lieuArrivee;
        private double distanceKm;
        private Reservation reservation; // Réservation associée à cette étape

        public TrajetEtape(String lieuDepart, String lieuArrivee, double distanceKm, Reservation reservation) {
            this.lieuDepart = lieuDepart;
            this.lieuArrivee = lieuArrivee;
            this.distanceKm = distanceKm;
            this.reservation = reservation;
        }

        public String getLieuDepart() {
            return lieuDepart;
        }

        public void setLieuDepart(String lieuDepart) {
            this.lieuDepart = lieuDepart;
        }

        public String getLieuArrivee() {
            return lieuArrivee;
        }

        public void setLieuArrivee(String lieuArrivee) {
            this.lieuArrivee = lieuArrivee;
        }

        public double getDistanceKm() {
            return distanceKm;
        }

        public void setDistanceKm(double distanceKm) {
            this.distanceKm = distanceKm;
        }

        public Reservation getReservation() {
            return reservation;
        }

        public void setReservation(Reservation reservation) {
            this.reservation = reservation;
        }
    }
}
