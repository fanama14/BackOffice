package com.backoffice.dao;

import com.backoffice.database.DatabaseConnection;
import com.backoffice.model.Aeroport;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AeroportDAO {

    public void insert(Aeroport aeroport) throws SQLException {
        String sql = "INSERT INTO aeroport (code, libelle, lieux_id) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, aeroport.getCode());
            ps.setString(2, aeroport.getLibelle());
            ps.setInt(3, aeroport.getLieuxId());
            ps.executeUpdate();
        }
    }

    public void update(Aeroport aeroport) throws SQLException {
        String sql = "UPDATE aeroport SET code = ?, libelle = ?, lieux_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, aeroport.getCode());
            ps.setString(2, aeroport.getLibelle());
            ps.setInt(3, aeroport.getLieuxId());
            ps.setInt(4, aeroport.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM aeroport WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Aeroport> findAll() throws SQLException {
        List<Aeroport> aeroports = new ArrayList<>();
        String sql = "SELECT a.id, a.code, a.libelle, a.lieux_id, l.lieu as lieux_nom " +
                     "FROM aeroport a " +
                     "LEFT JOIN lieux l ON a.lieux_id = l.id " +
                     "ORDER BY a.code";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Aeroport a = mapResultSetToAeroport(rs);
                aeroports.add(a);
            }
        }
        return aeroports;
    }

    public Aeroport findById(int id) throws SQLException {
        String sql = "SELECT a.id, a.code, a.libelle, a.lieux_id, l.lieu as lieux_nom " +
                     "FROM aeroport a " +
                     "LEFT JOIN lieux l ON a.lieux_id = l.id " +
                     "WHERE a.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAeroport(rs);
                }
            }
        }
        return null;
    }

    private Aeroport mapResultSetToAeroport(ResultSet rs) throws SQLException {
        Aeroport a = new Aeroport();
        a.setId(rs.getInt("id"));
        a.setCode(rs.getString("code"));
        a.setLibelle(rs.getString("libelle"));
        a.setLieuxId(rs.getInt("lieux_id"));
        a.setLieuxNom(rs.getString("lieux_nom"));
        return a;
    }
}
