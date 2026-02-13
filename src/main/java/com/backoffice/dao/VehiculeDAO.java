package com.backoffice.dao;

import com.backoffice.database.DatabaseConnection;
import com.backoffice.model.Vehicule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeDAO {

    /**
     * Insère un nouveau véhicule
     */
    public void insert(Vehicule vehicule) throws SQLException {
        String sql = "INSERT INTO vehicule (reference, nombre_place, type_carburant) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, vehicule.getReference());
            ps.setInt(2, vehicule.getNombrePlace());
            ps.setString(3, vehicule.getTypeCarburant());
            ps.executeUpdate();
        }
    }

    /**
     * Met à jour un véhicule existant
     */
    public void update(Vehicule vehicule) throws SQLException {
        String sql = "UPDATE vehicule SET reference = ?, nombre_place = ?, type_carburant = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, vehicule.getReference());
            ps.setInt(2, vehicule.getNombrePlace());
            ps.setString(3, vehicule.getTypeCarburant());
            ps.setInt(4, vehicule.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Supprime un véhicule par son ID
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicule WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Récupère tous les véhicules
     */
    public List<Vehicule> findAll() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT id, reference, nombre_place, type_carburant FROM vehicule ORDER BY reference";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Vehicule v = mapResultSetToVehicule(rs);
                vehicules.add(v);
            }
        }
        return vehicules;
    }

    /**
     * Recherche un véhicule par son ID
     */
    public Vehicule findById(int id) throws SQLException {
        String sql = "SELECT id, reference, nombre_place, type_carburant FROM vehicule WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVehicule(rs);
                }
            }
        }
        return null;
    }

    /**
     * Recherche des véhicules avec filtres
     * @param search Recherche textuelle sur la référence
     * @param typeCarburant Filtre par type de carburant (D, ES, H, EL)
     * @param nombrePlaceMin Nombre de places minimum
     * @param nombrePlaceMax Nombre de places maximum
     */
    public List<Vehicule> findWithFilters(String search, String typeCarburant, 
            Integer nombrePlaceMin, Integer nombrePlaceMax) throws SQLException {
        
        List<Vehicule> vehicules = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT id, reference, nombre_place, type_carburant FROM vehicule WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        // Filtre recherche textuelle
        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND LOWER(reference) LIKE LOWER(?)");
            params.add("%" + search.trim() + "%");
        }

        // Filtre type carburant
        if (typeCarburant != null && !typeCarburant.trim().isEmpty()) {
            sql.append(" AND type_carburant = ?");
            params.add(typeCarburant.trim());
        }

        // Filtre nombre de places minimum
        if (nombrePlaceMin != null) {
            sql.append(" AND nombre_place >= ?");
            params.add(nombrePlaceMin);
        }

        // Filtre nombre de places maximum
        if (nombrePlaceMax != null) {
            sql.append(" AND nombre_place <= ?");
            params.add(nombrePlaceMax);
        }

        sql.append(" ORDER BY reference");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // Bind des paramètres
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    ps.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    ps.setInt(i + 1, (Integer) param);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSetToVehicule(rs));
                }
            }
        }
        return vehicules;
    }

    /**
     * Mappe un ResultSet vers un objet Vehicule
     */
    private Vehicule mapResultSetToVehicule(ResultSet rs) throws SQLException {
        Vehicule v = new Vehicule();
        v.setId(rs.getInt("id"));
        v.setReference(rs.getString("reference"));
        v.setNombrePlace(rs.getInt("nombre_place"));
        v.setTypeCarburant(rs.getString("type_carburant"));
        return v;
    }
}
