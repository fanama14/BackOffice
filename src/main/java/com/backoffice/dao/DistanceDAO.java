package com.backoffice.dao;

import com.backoffice.database.DatabaseConnection;
import com.backoffice.model.Distance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DistanceDAO {

    public void insert(Distance distance) throws SQLException {
        String sql = "INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, distance.getLieuxFrom());
            ps.setInt(2, distance.getLieuxTo());
            ps.setDouble(3, distance.getValeur());
            ps.executeUpdate();
        }
    }

    public void update(Distance distance) throws SQLException {
        String sql = "UPDATE distance SET lieux_from = ?, lieux_to = ?, valeur = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, distance.getLieuxFrom());
            ps.setInt(2, distance.getLieuxTo());
            ps.setDouble(3, distance.getValeur());
            ps.setInt(4, distance.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM distance WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Distance> findAll() throws SQLException {
        List<Distance> distances = new ArrayList<>();
        String sql = "SELECT d.id, d.lieux_from, d.lieux_to, d.valeur, " +
                     "lf.lieu as lieux_from_nom, lt.lieu as lieux_to_nom " +
                     "FROM distance d " +
                     "LEFT JOIN lieux lf ON d.lieux_from = lf.id " +
                     "LEFT JOIN lieux lt ON d.lieux_to = lt.id " +
                     "ORDER BY lf.lieu, lt.lieu";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Distance d = mapResultSetToDistance(rs);
                distances.add(d);
            }
        }
        return distances;
    }

    public Distance findById(int id) throws SQLException {
        String sql = "SELECT d.id, d.lieux_from, d.lieux_to, d.valeur, " +
                     "lf.lieu as lieux_from_nom, lt.lieu as lieux_to_nom " +
                     "FROM distance d " +
                     "LEFT JOIN lieux lf ON d.lieux_from = lf.id " +
                     "LEFT JOIN lieux lt ON d.lieux_to = lt.id " +
                     "WHERE d.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDistance(rs);
                }
            }
        }
        return null;
    }

    /**
     * Trouve la distance entre deux lieux (dans les deux sens)
     */
    public Distance findByLieux(int lieuxFromId, int lieuxToId) throws SQLException {
        String sql = "SELECT d.id, d.lieux_from, d.lieux_to, d.valeur, " +
                     "lf.lieu as lieux_from_nom, lt.lieu as lieux_to_nom " +
                     "FROM distance d " +
                     "LEFT JOIN lieux lf ON d.lieux_from = lf.id " +
                     "LEFT JOIN lieux lt ON d.lieux_to = lt.id " +
                     "WHERE (d.lieux_from = ? AND d.lieux_to = ?) " +
                     "   OR (d.lieux_from = ? AND d.lieux_to = ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lieuxFromId);
            ps.setInt(2, lieuxToId);
            ps.setInt(3, lieuxToId);
            ps.setInt(4, lieuxFromId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDistance(rs);
                }
            }
        }
        return null;
    }

    /**
     * Récupère la valeur de distance en km entre deux lieux
     * @return distance en km, ou 0 si non trouvée
     */
    public double getDistanceKm(int lieuxFromId, int lieuxToId) throws SQLException {
        Distance d = findByLieux(lieuxFromId, lieuxToId);
        return d != null ? d.getValeur() : 0;
    }

    private Distance mapResultSetToDistance(ResultSet rs) throws SQLException {
        Distance d = new Distance();
        d.setId(rs.getInt("id"));
        d.setLieuxFrom(rs.getInt("lieux_from"));
        d.setLieuxTo(rs.getInt("lieux_to"));
        d.setValeur(rs.getDouble("valeur"));
        d.setLieuxFromNom(rs.getString("lieux_from_nom"));
        d.setLieuxToNom(rs.getString("lieux_to_nom"));
        return d;
    }
}
