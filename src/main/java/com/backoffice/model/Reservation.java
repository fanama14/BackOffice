package com.backoffice.model;

import java.sql.Timestamp;

public class Reservation {
    private int id;
    private String clientId;
    private int nombrePassager;
    private Timestamp dateArrivee;
    private int hotelId;
    private int aeroportId;

    // Champs calculés pour l'affichage
    private String hotelNom;
    private String aeroportNom;

    // Champs calculés pour la simulation/planification
    private String vehiculeReference;
    private String vehiculeTypeCarburant;
    private int vehiculeNombrePlace;
    private Timestamp heureDepartAeroport;
    private Timestamp heureRetourAeroport;
    private double distanceKm;

    public Reservation() {
    }

    public Reservation(int id, String clientId, int nombrePassager, Timestamp dateArrivee, int hotelId, int aeroportId) {
        this.id = id;
        this.clientId = clientId;
        this.nombrePassager = nombrePassager;
        this.dateArrivee = dateArrivee;
        this.hotelId = hotelId;
        this.aeroportId = aeroportId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getNombrePassager() {
        return nombrePassager;
    }

    public void setNombrePassager(int nombrePassager) {
        this.nombrePassager = nombrePassager;
    }

    public Timestamp getDateArrivee() {
        return dateArrivee;
    }

    public void setDateArrivee(Timestamp dateArrivee) {
        this.dateArrivee = dateArrivee;
    }

    public int getHotelId() {
        return hotelId;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelNom() {
        return hotelNom;
    }

    public void setHotelNom(String hotelNom) {
        this.hotelNom = hotelNom;
    }

    public int getAeroportId() {
        return aeroportId;
    }

    public void setAeroportId(int aeroportId) {
        this.aeroportId = aeroportId;
    }

    public String getAeroportNom() {
        return aeroportNom;
    }

    public void setAeroportNom(String aeroportNom) {
        this.aeroportNom = aeroportNom;
    }

    public String getVehiculeReference() {
        return vehiculeReference;
    }

    public void setVehiculeReference(String vehiculeReference) {
        this.vehiculeReference = vehiculeReference;
    }

    public String getVehiculeTypeCarburant() {
        return vehiculeTypeCarburant;
    }

    public void setVehiculeTypeCarburant(String vehiculeTypeCarburant) {
        this.vehiculeTypeCarburant = vehiculeTypeCarburant;
    }

    public int getVehiculeNombrePlace() {
        return vehiculeNombrePlace;
    }

    public void setVehiculeNombrePlace(int vehiculeNombrePlace) {
        this.vehiculeNombrePlace = vehiculeNombrePlace;
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

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
