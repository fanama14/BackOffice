package com.backoffice.dao;

import com.backoffice.database.DatabaseConnection;
import com.backoffice.model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public void insert(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservation (client_id, nombre_passager, date_arrivee, hotel_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, reservation.getClientId());
            ps.setInt(2, reservation.getNombrePassager());
            ps.setTimestamp(3, reservation.getDateArrivee());
            ps.setInt(4, reservation.getHotelId());
            ps.executeUpdate();
        }
    }

    public List<Reservation> findAll() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT id, client_id, nombre_passager, date_arrivee, hotel_id FROM reservation ORDER BY date_arrivee DESC";

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
                reservations.add(r);
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
}
