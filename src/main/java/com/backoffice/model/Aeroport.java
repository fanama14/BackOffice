package com.backoffice.model;

public class Aeroport {
    private int id;
    private String code;
    private String libelle;
    private int lieuxId;

    // Champ calculé pour l'affichage
    private String lieuxNom;

    public Aeroport() {
    }

    public Aeroport(int id, String code, String libelle, int lieuxId) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.lieuxId = lieuxId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
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
