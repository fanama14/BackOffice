package com.backoffice.model;

public class Lieux {
    private int id;
    private String lieu;

    public Lieux() {
    }

    public Lieux(int id, String lieu) {
        this.id = id;
        this.lieu = lieu;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }
}
