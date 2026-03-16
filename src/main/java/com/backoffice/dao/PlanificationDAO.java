package com.backoffice.dao;

import com.backoffice.database.DatabaseConnection;
import com.backoffice.model.Planification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanificationDAO {

    public void deleteByPeriode(Timestamp dateDebut, Timestamp dateFin) throws SQLException {
        String sql = "DELETE FROM planification WHERE reservation_id IN (" +
                     "  SELECT r.id FROM reservation r WHERE r.date_arrivee >= ? AND r.date_arrivee <= ?" +
                     ")";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, dateDebut);
            ps.setTimestamp(2, dateFin);
            ps.executeUpdate();
        }
    }

    public void insertBatch(List<Planification> planifications) throws SQLException {
        String sql = "INSERT INTO planification (reservation_id, vehicule_id, groupe_id, ordre_livraison, heure_depart, heure_retour) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Planification p : planifications) {
                ps.setInt(1, p.getReservationId());
                if (p.getVehiculeId() != null) {
                    ps.setInt(2, p.getVehiculeId());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setInt(3, p.getGroupeId());
                ps.setInt(4, p.getOrdreLivraison());
                if (p.getHeureDepart() != null) {
                    ps.setTimestamp(5, p.getHeureDepart());
                } else {
                    ps.setNull(5, Types.TIMESTAMP);
                }
                if (p.getHeureRetour() != null) {
                    ps.setTimestamp(6, p.getHeureRetour());
                } else {
                    ps.setNull(6, Types.TIMESTAMP);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Planification> findByPeriode(Timestamp dateDebut, Timestamp dateFin) throws SQLException {
        List<Planification> list = new ArrayList<>();
        String sql = "SELECT p.id, p.reservation_id, p.vehicule_id, p.groupe_id, p.ordre_livraison, " +
                     "p.heure_depart, p.heure_retour " +
                     "FROM planification p " +
                     "JOIN reservation r ON p.reservation_id = r.id " +
                     "WHERE r.date_arrivee >= ? AND r.date_arrivee <= ? " +
                     "ORDER BY p.groupe_id, p.ordre_livraison";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, dateDebut);
            ps.setTimestamp(2, dateFin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Planification p = new Planification();
                    p.setId(rs.getInt("id"));
                    p.setReservationId(rs.getInt("reservation_id"));
                    int vid = rs.getInt("vehicule_id");
                    p.setVehiculeId(rs.wasNull() ? null : vid);
                    p.setGroupeId(rs.getInt("groupe_id"));
                    p.setOrdreLivraison(rs.getInt("ordre_livraison"));
                    p.setHeureDepart(rs.getTimestamp("heure_depart"));
                    p.setHeureRetour(rs.getTimestamp("heure_retour"));
                    list.add(p);
                }
            }
        }
        return list;
    }
}
