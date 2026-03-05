package com.backoffice.model;

public class Hotel {
    private int id;
    private String nom;
    private String adresse;
    private String ville;
    private int lieuxId;

    // Champ calculé pour l'affichage
    private String lieuxNom;

    public Hotel() {
    }

    public Hotel(int id, String nom, String adresse, String ville, int lieuxId) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.ville = ville;
        this.lieuxId = lieuxId;
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

    public int getLieuxId() {
        return lieuxId;
    }

    public void setLieuxId(int lieuxId) {
        this.lieuxId = lieuxId;
    }

    public String getLieuxNom() {
        return lieuxNom;
    }

    public void setLieuxNom(String lieuxNom) {
        this.lieuxNom = lieuxNom;
    }
}
