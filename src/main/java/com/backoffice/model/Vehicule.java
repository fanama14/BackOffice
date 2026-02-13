package com.backoffice.model;

public class Vehicule {
    private int id;
    private String reference;
    private int nombrePlace;
    private String typeCarburant;

    public Vehicule() {
    }

    public Vehicule(int id, String reference, int nombrePlace, String typeCarburant) {
        this.id = id;
        this.reference = reference;
        this.nombrePlace = nombrePlace;
        this.typeCarburant = typeCarburant;
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

    /**
     * Retourne le libellé du type de carburant
     */
    public String getTypeCarburantLibelle() {
        switch (typeCarburant) {
            case "D": return "Diesel";
            case "ES": return "Essence";
            case "H": return "Hybride";
            case "EL": return "Électrique";
            default: return typeCarburant;
        }
    }
}
