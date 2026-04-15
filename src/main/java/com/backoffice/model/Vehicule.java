package com.backoffice.model;

import java.sql.Time;

public class Vehicule {
    private int id;
    private String reference;
    private int nombrePlace;
    private String typeCarburant;
    private Time heureDisponibilite;

    public Vehicule() {
        this.heureDisponibilite = Time.valueOf("00:00:00");
    }

    public Vehicule(int id, String reference, int nombrePlace, String typeCarburant) {
        this.id = id;
        this.reference = reference;
        this.nombrePlace = nombrePlace;
        this.typeCarburant = typeCarburant;
        this.heureDisponibilite = Time.valueOf("00:00:00");
    }

    public Vehicule(int id, String reference, int nombrePlace, String typeCarburant, Time heureDisponibilite) {
        this.id = id;
        this.reference = reference;
        this.nombrePlace = nombrePlace;
        this.typeCarburant = typeCarburant;
        this.heureDisponibilite = heureDisponibilite != null ? heureDisponibilite : Time.valueOf("00:00:00");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public int getNombrePlace() {
        return nombrePlace;
    }

    public void setNombrePlace(int nombrePlace) {
        this.nombrePlace = nombrePlace;
    }

    public String getTypeCarburant() {
        return typeCarburant;
    }

    public void setTypeCarburant(String typeCarburant) {
        this.typeCarburant = typeCarburant;
    }

    public Time getHeureDisponibilite() {
        return heureDisponibilite;
    }

    public void setHeureDisponibilite(Time heureDisponibilite) {
        this.heureDisponibilite = heureDisponibilite != null ? heureDisponibilite : Time.valueOf("00:00:00");
    }

    /**
     * Retourne le libellé du type de carburant
     */
    public String getTypeCarburantLibelle() {
        switch (typeCarburant) {
            case "D":
                return "Diesel";
            case "ES":
                return "Essence";
            case "H":
                return "Hybride";
            case "EL":
                return "Électrique";
            default:
                return typeCarburant;
        }
    }
}
