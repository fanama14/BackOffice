package com.backoffice.model;

public class Parametre {
    private int id;
    private int tempsAttente;      // en minutes (temps d'attente à l'aéroport avant départ)
    private int vitesseMoyenne;    // en km/h

    public Parametre() {
    }

    public Parametre(int id, int tempsAttente, int vitesseMoyenne) {
        this.id = id;
        this.tempsAttente = tempsAttente;
        this.vitesseMoyenne = vitesseMoyenne;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTempsAttente() {
        return tempsAttente;
    }

    public void setTempsAttente(int tempsAttente) {
        this.tempsAttente = tempsAttente;
    }

    public int getVitesseMoyenne() {
        return vitesseMoyenne;
    }

    public void setVitesseMoyenne(int vitesseMoyenne) {
        this.vitesseMoyenne = vitesseMoyenne;
    }

    /**
     * Calcule le temps de trajet en minutes pour une distance donnée
     * @param distanceKm distance en kilomètres
     * @return temps en minutes
     */
    public int calculerTempsTrajet(double distanceKm) {
        if (vitesseMoyenne <= 0) return 0;
        return (int) Math.ceil((distanceKm / vitesseMoyenne) * 60);
    }
}
