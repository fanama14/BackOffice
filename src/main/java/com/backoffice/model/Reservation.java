package com.backoffice.model;

import java.sql.Timestamp;

public class Reservation {
    private int id;
    private String clientId;
    private int nombrePassager;
    private Timestamp dateArrivee;
    private int hotelId;

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
}
