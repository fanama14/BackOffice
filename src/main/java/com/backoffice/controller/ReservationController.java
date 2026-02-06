package com.backoffice.controller;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.annotations.POST;
import mg.framework.annotations.RequestParam;
import mg.framework.annotations.RestAPI;
import mg.framework.ModelView;

import com.backoffice.dao.HotelDAO;
import com.backoffice.dao.ReservationDAO;
import com.backoffice.model.Reservation;
import com.backoffice.model.Hotel;

import java.sql.Timestamp;
import java.util.List;

@Controller
public class ReservationController {

    private final HotelDAO hotelDAO = new HotelDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();

    /**
     * Affiche le formulaire de réservation
     */
    @GET("reservation/form")
    public ModelView showForm() {
        ModelView mv = new ModelView("reservation-form");
        try {
            List<Hotel> hotels = hotelDAO.findAll();
            mv.addData("hotels", hotels);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement des hôtels : " + e.getMessage());
        }
        return mv;
    }

    /**
     * Enregistre une réservation et retourne au formulaire
     */
    @POST("reservation/save")
    public ModelView save(@RequestParam("clientId") String clientId,
            @RequestParam("nombrePassager") int nombrePassager,
            @RequestParam("dateArrivee") String dateArrivee,
            @RequestParam("hotelId") int hotelId) {
        ModelView mv = new ModelView("reservation-form");
        try {
            Reservation reservation = new Reservation();
            reservation.setClientId(clientId);
            reservation.setNombrePassager(nombrePassager);
            reservation.setDateArrivee(Timestamp.valueOf(dateArrivee.replace("T", " ") + ":00"));
            reservation.setHotelId(hotelId);

            reservationDAO.insert(reservation);
            mv.addData("success", "Réservation enregistrée avec succès !");

        } catch (Exception e) {
            mv.addData("error", "Erreur lors de l'enregistrement : " + e.getMessage());
        }

        // Recharger la liste des hôtels pour le formulaire
        try {
            List<Hotel> hotels = hotelDAO.findAll();
            mv.addData("hotels", hotels);
        } catch (Exception e) {
            // ignore
        }

        return mv;
    }

    /**
     * API REST: Retourne la liste des réservations en JSON
     */
    @GET("api/reservation/list")
    @RestAPI
    public List<Reservation> listJSON() {
        try {
            return reservationDAO.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des réservations", e);
        }
    }
}
