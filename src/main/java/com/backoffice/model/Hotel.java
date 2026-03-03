package com.backoffice.model;

public class Hotel {
    private int id;
    private String nom;
    private String adresse;
    private String ville;
    private String telephone;
    private double distanceAeroport;  // distance en km depuis l'aéroport

    public Hotel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public double getDistanceAeroport() {
        return distanceAeroport;
    }

    public void setDistanceAeroport(double distanceAeroport) {
        this.distanceAeroport = distanceAeroport;
    }
}
