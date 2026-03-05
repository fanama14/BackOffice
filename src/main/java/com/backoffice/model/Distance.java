package com.backoffice.model;

public class Distance {
    private int id;
    private int lieuxFrom;
    private int lieuxTo;
    private double valeur; // distance en km

    // Champs calculés pour l'affichage
    private String lieuxFromNom;
    private String lieuxToNom;

    public Distance() {
    }

    public Distance(int id, int lieuxFrom, int lieuxTo, double valeur) {
        this.id = id;
        this.lieuxFrom = lieuxFrom;
        this.lieuxTo = lieuxTo;
        this.valeur = valeur;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLieuxFrom() {
        return lieuxFrom;
    }

    public void setLieuxFrom(int lieuxFrom) {
        this.lieuxFrom = lieuxFrom;
    }

    public int getLieuxTo() {
        return lieuxTo;
    }

    public void setLieuxTo(int lieuxTo) {
        this.lieuxTo = lieuxTo;
    }

    public double getValeur() {
        return valeur;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public String getLieuxFromNom() {
        return lieuxFromNom;
    }

    public void setLieuxFromNom(String lieuxFromNom) {
        this.lieuxFromNom = lieuxFromNom;
    }

    public String getLieuxToNom() {
        return lieuxToNom;
    }

    public void setLieuxToNom(String lieuxToNom) {
        this.lieuxToNom = lieuxToNom;
    }
}
