package com.backoffice.dao;

import com.backoffice.database.DatabaseConnection;
import com.backoffice.model.Hotel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HotelDAO {

    public List<Hotel> findAll() throws SQLException {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT id, nom, adresse, ville, telephone FROM hotel ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Hotel h = new Hotel();
                h.setId(rs.getInt("id"));
                h.setNom(rs.getString("nom"));
                h.setAdresse(rs.getString("adresse"));
                h.setVille(rs.getString("ville"));
                h.setTelephone(rs.getString("telephone"));
                hotels.add(h);
            }
        }
        return hotels;
    }

    public Hotel findById(int id) throws SQLException {
        String sql = "SELECT id, nom, adresse, ville, telephone FROM hotel WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Hotel h = new Hotel();
                    h.setId(rs.getInt("id"));
                    h.setNom(rs.getString("nom"));
                    h.setAdresse(rs.getString("adresse"));
                    h.setVille(rs.getString("ville"));
                    h.setTelephone(rs.getString("telephone"));
                    return h;
                }
            }
        }
        return null;
    }
}
