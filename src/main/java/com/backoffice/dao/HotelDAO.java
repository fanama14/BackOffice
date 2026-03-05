package com.backoffice.dao;

import com.backoffice.database.DatabaseConnection;
import com.backoffice.model.Hotel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HotelDAO {

    public void insert(Hotel hotel) throws SQLException {
        String sql = "INSERT INTO hotel (nom, adresse, ville, lieux_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hotel.getNom());
            ps.setString(2, hotel.getAdresse());
            ps.setString(3, hotel.getVille());
            ps.setInt(4, hotel.getLieuxId());
            ps.executeUpdate();
        }
    }

    public void update(Hotel hotel) throws SQLException {
        String sql = "UPDATE hotel SET nom = ?, adresse = ?, ville = ?, lieux_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hotel.getNom());
            ps.setString(2, hotel.getAdresse());
            ps.setString(3, hotel.getVille());
            ps.setInt(4, hotel.getLieuxId());
            ps.setInt(5, hotel.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM hotel WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Hotel> findAll() throws SQLException {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT h.id, h.nom, h.adresse, h.ville, h.lieux_id, l.lieu as lieux_nom " +
                     "FROM hotel h " +
                     "LEFT JOIN lieux l ON h.lieux_id = l.id " +
                     "ORDER BY h.nom";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Hotel h = mapResultSetToHotel(rs);
                hotels.add(h);
            }
        }
        return hotels;
    }

    public Hotel findById(int id) throws SQLException {
        String sql = "SELECT h.id, h.nom, h.adresse, h.ville, h.lieux_id, l.lieu as lieux_nom " +
                     "FROM hotel h " +
                     "LEFT JOIN lieux l ON h.lieux_id = l.id " +
                     "WHERE h.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToHotel(rs);
                }
            }
        }
        return null;
    }

    private Hotel mapResultSetToHotel(ResultSet rs) throws SQLException {
        Hotel h = new Hotel();
        h.setId(rs.getInt("id"));
        h.setNom(rs.getString("nom"));
        h.setAdresse(rs.getString("adresse"));
        h.setVille(rs.getString("ville"));
        h.setLieuxId(rs.getInt("lieux_id"));
        h.setLieuxNom(rs.getString("lieux_nom"));
        return h;
    }
}
