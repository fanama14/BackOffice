package com.backoffice.dao;

import com.backoffice.database.DatabaseConnection;
import com.backoffice.model.Reservation;
import com.backoffice.model.Parametre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public void insert(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservation (client_id, nombre_passager, date_arrivee, hotel_id, id_vehicule) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservation.getClientId());
            ps.setInt(2, reservation.getNombrePassager());
            ps.setTimestamp(3, reservation.getDateArrivee());
            ps.setInt(4, reservation.getHotelId());
            if (reservation.getIdVehicule() != null) {
                ps.setInt(5, reservation.getIdVehicule());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
        }
    }

    public List<Reservation> findAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT id, client_id, nombre_passager, date_arrivee, hotel_id, id_vehicule FROM reservation ORDER BY date_arrivee DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setClientId(rs.getString("client_id"));
                r.setNombrePassager(rs.getInt("nombre_passager"));
                r.setDateArrivee(rs.getTimestamp("date_arrivee"));
                r.setHotelId(rs.getInt("hotel_id"));
                r.setIdVehicule(rs.getObject("id_vehicule") != null ? rs.getInt("id_vehicule") : null);
                reservations.add(r);
            }
        }
        return reservations;
    }

    /**
     * Récupère une réservation par son ID
     */
    public Reservation findById(int id) throws SQLException {
        String sql = "SELECT id, client_id, nombre_passager, date_arrivee, hotel_id, id_vehicule FROM reservation WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Reservation r = new Reservation();
                    r.setId(rs.getInt("id"));
                    r.setClientId(rs.getString("client_id"));
                    r.setNombrePassager(rs.getInt("nombre_passager"));
                    r.setDateArrivee(rs.getTimestamp("date_arrivee"));
                    r.setHotelId(rs.getInt("hotel_id"));
                    r.setIdVehicule(rs.getObject("id_vehicule") != null ? rs.getInt("id_vehicule") : null);
                    return r;
                }
            }
        }
        return null;
    }

    /**
     * Récupère les réservations pour la planification avec les informations de l'hôtel et du véhicule
     * Filtrées par période
     */
    public List<Reservation> findForPlanification(Timestamp dateDebut, Timestamp dateFin, Parametre parametre) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.id, r.client_id, r.nombre_passager, r.date_arrivee, r.hotel_id, r.id_vehicule, " +
                     "h.nom as hotel_nom, h.distance_aeroport, v.reference as vehicule_reference " +
                     "FROM reservation r " +
                     "JOIN hotel h ON r.hotel_id = h.id " +
                     "LEFT JOIN vehicule v ON r.id_vehicule = v.id " +
                     "WHERE r.date_arrivee >= ? AND r.date_arrivee <= ? " +
                     "ORDER BY r.date_arrivee ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, dateDebut);
            ps.setTimestamp(2, dateFin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reservation r = new Reservation();
                    r.setId(rs.getInt("id"));
                    r.setClientId(rs.getString("client_id"));
                    r.setNombrePassager(rs.getInt("nombre_passager"));
                    r.setDateArrivee(rs.getTimestamp("date_arrivee"));
                    r.setHotelId(rs.getInt("hotel_id"));
                    r.setIdVehicule(rs.getObject("id_vehicule") != null ? rs.getInt("id_vehicule") : null);
                    r.setHotelNom(rs.getString("hotel_nom"));
                    r.setVehiculeReference(rs.getString("vehicule_reference"));

                    // Calcul des heures de départ et de retour à l'aéroport si un véhicule est assigné
                    if (r.getIdVehicule() != null && parametre != null) {
                        double distanceKm = rs.getDouble("distance_aeroport");
                        int tempsTrajetMinutes = parametre.calculerTempsTrajet(distanceKm);
                        
                        // Heure de départ aéroport = date arrivée + temps d'attente
                        long heureDepartMs = r.getDateArrivee().getTime() + (parametre.getTempsAttente() * 60 * 1000L);
                        r.setHeureDepartAeroport(new Timestamp(heureDepartMs));
                        
                        // Heure de retour à l'aéroport = heure départ + temps de trajet * 2 (aller-retour)
                        long heureRetourMs = heureDepartMs + (tempsTrajetMinutes * 60 * 1000L * 2);
                        r.setHeureArriveeAeroport(new Timestamp(heureRetourMs));
                    }

                    reservations.add(r);
                }
            }
        }
        return reservations;
    }

    /**
     * Assigne un véhicule à une réservation
     */
    public void assignerVehicule(int reservationId, int vehiculeId) throws SQLException {
        String sql = "UPDATE reservation SET id_vehicule = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehiculeId);
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        }
    }

    /**
     * Retire l'assignation d'un véhicule d'une réservation
     */
    public void retirerVehicule(int reservationId) throws SQLException {
        String sql = "UPDATE reservation SET id_vehicule = NULL WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reservation WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
