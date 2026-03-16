package com.backoffice.model;

import java.sql.Timestamp;

public class Planification {
    private int id;
    private int reservationId;
    private Integer vehiculeId;
    private int groupeId;
    private int ordreLivraison;
    private Timestamp heureDepart;
    private Timestamp heureRetour;

    public Planification() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public Integer getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(Integer vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public int getGroupeId() {
        return groupeId;
    }

    public void setGroupeId(int groupeId) {
        this.groupeId = groupeId;
    }

    public int getOrdreLivraison() {
        return ordreLivraison;
    }

    public void setOrdreLivraison(int ordreLivraison) {
        this.ordreLivraison = ordreLivraison;
    }

    public Timestamp getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(Timestamp heureDepart) {
        this.heureDepart = heureDepart;
    }

    public Timestamp getHeureRetour() {
        return heureRetour;
    }

    public void setHeureRetour(Timestamp heureRetour) {
        this.heureRetour = heureRetour;
    }
}
