package com.backoffice.dao;

import com.backoffice.database.DatabaseConnection;
import com.backoffice.model.Planification;
import com.backoffice.model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String sql = "INSERT INTO planification (reservation_id, nb_passagers_affectes, vehicule_id, groupe_id, ordre_livraison, heure_depart, heure_retour) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Planification p : planifications) {
                ps.setInt(1, p.getReservationId());
                ps.setInt(2, p.getNbPassagersAffectes());
                if (p.getVehiculeId() != null) {
                    ps.setInt(3, p.getVehiculeId());
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                ps.setInt(4, p.getGroupeId());
                ps.setInt(5, p.getOrdreLivraison());
                if (p.getHeureDepart() != null) {
                    ps.setTimestamp(6, p.getHeureDepart());
                } else {
                    ps.setNull(6, Types.TIMESTAMP);
                }
                if (p.getHeureRetour() != null) {
                    ps.setTimestamp(7, p.getHeureRetour());
                } else {
                    ps.setNull(7, Types.TIMESTAMP);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Planification> findByPeriode(Timestamp dateDebut, Timestamp dateFin) throws SQLException {
        List<Planification> list = new ArrayList<>();
        String sql = "SELECT p.id, p.reservation_id, p.nb_passagers_affectes, p.vehicule_id, p.groupe_id, p.ordre_livraison, " +
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
                    p.setNbPassagersAffectes(rs.getInt("nb_passagers_affectes"));
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

    public List<Reservation> findReservationsByPeriode(Timestamp dateDebut, Timestamp dateFin) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.id, r.client_id, p.nb_passagers_affectes AS nombre_passager, r.date_arrivee, r.hotel_id, r.aeroport_id, " +
                "h.nom AS hotel_nom, a.libelle AS aeroport_nom, " +
                "p.groupe_id, p.ordre_livraison, p.heure_depart, p.heure_retour, " +
                "v.reference AS vehicule_reference, v.nombre_place AS vehicule_nombre_place, " +
                "CASE v.type_carburant " +
                "    WHEN 'D' THEN 'Diesel' " +
                "    WHEN 'ES' THEN 'Essence' " +
                "    WHEN 'H' THEN 'Hybride' " +
                "    WHEN 'EL' THEN 'Electrique' " +
                "    ELSE v.type_carburant " +
                "END AS vehicule_type_carburant, " +
                "COALESCE(d1.valeur, d2.valeur, 0) AS distance_km " +
                "FROM planification p " +
                "JOIN reservation r ON p.reservation_id = r.id " +
                "LEFT JOIN hotel h ON r.hotel_id = h.id " +
                "LEFT JOIN aeroport a ON r.aeroport_id = a.id " +
                "LEFT JOIN vehicule v ON p.vehicule_id = v.id " +
                "LEFT JOIN distance d1 ON d1.lieux_from = a.lieux_id AND d1.lieux_to = h.lieux_id " +
                "LEFT JOIN distance d2 ON d2.lieux_from = h.lieux_id AND d2.lieux_to = a.lieux_id " +
                "WHERE r.date_arrivee >= ? AND r.date_arrivee <= ? " +
                "ORDER BY p.groupe_id ASC, p.ordre_livraison ASC, r.id ASC";

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
                    r.setAeroportId(rs.getInt("aeroport_id"));
                    r.setHotelNom(rs.getString("hotel_nom"));
                    r.setAeroportNom(rs.getString("aeroport_nom"));

                    r.setGroupeId(rs.getInt("groupe_id"));
                    r.setOrdreLivraison(rs.getInt("ordre_livraison"));
                    r.setHeureDepartAeroport(rs.getTimestamp("heure_depart"));
                    r.setHeureRetourAeroport(rs.getTimestamp("heure_retour"));

                    String vehiculeReference = rs.getString("vehicule_reference");
                    if (vehiculeReference != null) {
                        r.setVehiculeReference(vehiculeReference);
                        r.setVehiculeNombrePlace(rs.getInt("vehicule_nombre_place"));
                        r.setVehiculeTypeCarburant(rs.getString("vehicule_type_carburant"));
                    }

                    r.setDistanceKm(rs.getDouble("distance_km"));
                    reservations.add(r);
                }
            }
        }

        return reservations;
    }

    public Map<Integer, Integer> countTripsByVehicule() throws SQLException {
        Map<Integer, Integer> counts = new HashMap<>();
        String sql = "SELECT vehicule_id, COUNT(DISTINCT groupe_id) AS trip_count " +
                     "FROM planification " +
                     "WHERE vehicule_id IS NOT NULL " +
                     "GROUP BY vehicule_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                counts.put(rs.getInt("vehicule_id"), rs.getInt("trip_count"));
            }
        }

        return counts;
    }
}
