package com.backoffice.dao;

import com.backoffice.database.DatabaseConnection;
import com.backoffice.model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public void insert(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservation (client_id, nombre_passager, date_arrivee, hotel_id, aeroport_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservation.getClientId());
            ps.setInt(2, reservation.getNombrePassager());
            ps.setTimestamp(3, reservation.getDateArrivee());
            ps.setInt(4, reservation.getHotelId());
            ps.setInt(5, reservation.getAeroportId());
            ps.executeUpdate();
        }
    }

    public void update(Reservation reservation) throws SQLException {
        String sql = "UPDATE reservation SET client_id = ?, nombre_passager = ?, date_arrivee = ?, hotel_id = ?, aeroport_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservation.getClientId());
            ps.setInt(2, reservation.getNombrePassager());
            ps.setTimestamp(3, reservation.getDateArrivee());
            ps.setInt(4, reservation.getHotelId());
            ps.setInt(5, reservation.getAeroportId());
            ps.setInt(6, reservation.getId());
            ps.executeUpdate();
        }
    }

    public List<Reservation> findAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.id, r.client_id, r.nombre_passager, r.date_arrivee, r.hotel_id, r.aeroport_id, " +
                     "h.nom as hotel_nom, a.libelle as aeroport_nom " +
                     "FROM reservation r " +
                     "LEFT JOIN hotel h ON r.hotel_id = h.id " +
                     "LEFT JOIN aeroport a ON r.aeroport_id = a.id " +
                     "ORDER BY r.date_arrivee DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Reservation r = mapResultSetToReservation(rs);
                reservations.add(r);
            }
        }
        return reservations;
    }

    public Reservation findById(int id) throws SQLException {
        String sql = "SELECT r.id, r.client_id, r.nombre_passager, r.date_arrivee, r.hotel_id, r.aeroport_id, " +
                     "h.nom as hotel_nom, a.libelle as aeroport_nom " +
                     "FROM reservation r " +
                     "LEFT JOIN hotel h ON r.hotel_id = h.id " +
                     "LEFT JOIN aeroport a ON r.aeroport_id = a.id " +
                     "WHERE r.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
            }
        }
        return null;
    }

    /**
     * Récupère les réservations filtrées par période
     */
    public List<Reservation> findByPeriode(Timestamp dateDebut, Timestamp dateFin) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.id, r.client_id, r.nombre_passager, r.date_arrivee, r.hotel_id, r.aeroport_id, " +
                     "h.nom as hotel_nom, a.libelle as aeroport_nom " +
                     "FROM reservation r " +
                     "LEFT JOIN hotel h ON r.hotel_id = h.id " +
                     "LEFT JOIN aeroport a ON r.aeroport_id = a.id " +
                     "WHERE r.date_arrivee >= ? AND r.date_arrivee <= ? " +
                     "ORDER BY r.date_arrivee ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, dateDebut);
            ps.setTimestamp(2, dateFin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reservation r = mapResultSetToReservation(rs);
                    reservations.add(r);
                }
            }
        }
        return reservations;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reservation WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setClientId(rs.getString("client_id"));
        r.setNombrePassager(rs.getInt("nombre_passager"));
        r.setDateArrivee(rs.getTimestamp("date_arrivee"));
        r.setHotelId(rs.getInt("hotel_id"));
        r.setAeroportId(rs.getInt("aeroport_id"));
        r.setHotelNom(rs.getString("hotel_nom"));
        r.setAeroportNom(rs.getString("aeroport_nom"));
        return r;
    }
}
