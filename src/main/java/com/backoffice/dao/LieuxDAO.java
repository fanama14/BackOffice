package com.backoffice.dao;

import com.backoffice.database.DatabaseConnection;
import com.backoffice.model.Lieux;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LieuxDAO {

    public void insert(Lieux lieux) throws SQLException {
        String sql = "INSERT INTO lieux (lieu) VALUES (?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lieux.getLieu());
            ps.executeUpdate();
        }
    }

    public void update(Lieux lieux) throws SQLException {
        String sql = "UPDATE lieux SET lieu = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lieux.getLieu());
            ps.setInt(2, lieux.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM lieux WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Lieux> findAll() throws SQLException {
        List<Lieux> lieuxList = new ArrayList<>();
        String sql = "SELECT id, lieu FROM lieux ORDER BY lieu";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Lieux l = new Lieux();
                l.setId(rs.getInt("id"));
                l.setLieu(rs.getString("lieu"));
                lieuxList.add(l);
            }
        }
        return lieuxList;
    }

    public Lieux findById(int id) throws SQLException {
        String sql = "SELECT id, lieu FROM lieux WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Lieux l = new Lieux();
                    l.setId(rs.getInt("id"));
                    l.setLieu(rs.getString("lieu"));
                    return l;
                }
            }
        }
        return null;
    }
}
