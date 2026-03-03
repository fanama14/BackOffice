package com.backoffice.model;

import java.sql.Timestamp;

public class Reservation {
    private int id;
    private String clientId;
    private int nombrePassager;
    private Timestamp dateArrivee;
    private int hotelId;
    private Integer idVehicule;
    
    // Champs calculés pour l'affichage dans la planification
    private String hotelNom;
    private String vehiculeReference;
    private Timestamp heureDepartAeroport;
    private Timestamp heureArriveeAeroport;

    public Reservation() {
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

    public Integer getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(Integer idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getHotelNom() {
        return hotelNom;
    }

    public void setHotelNom(String hotelNom) {
        this.hotelNom = hotelNom;
    }

    public String getVehiculeReference() {
        return vehiculeReference;
    }

    public void setVehiculeReference(String vehiculeReference) {
        this.vehiculeReference = vehiculeReference;
    }

    public Timestamp getHeureDepartAeroport() {
        return heureDepartAeroport;
    }

    public void setHeureDepartAeroport(Timestamp heureDepartAeroport) {
        this.heureDepartAeroport = heureDepartAeroport;
    }

    public Timestamp getHeureArriveeAeroport() {
        return heureArriveeAeroport;
    }

    public void setHeureArriveeAeroport(Timestamp heureArriveeAeroport) {
        this.heureArriveeAeroport = heureArriveeAeroport;
    }
}
