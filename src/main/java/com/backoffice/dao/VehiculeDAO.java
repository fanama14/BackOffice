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
     * Récupère les véhicules disponibles pour une réservation, triés par règles d'assignation :
     * 1. nombre_place >= nombrePassager, le plus proche en premier
     * 2. Priorité carburant : D > ES > H > EL
     * 
     * @param nombrePassager Nombre de passagers minimum requis
     * @return Liste des véhicules triés par pertinence
     */
    public List<Vehicule> findBestVehicles(int nombrePassager) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        
        String sql = "SELECT v.id, v.reference, v.nombre_place, v.type_carburant " +
                     "FROM vehicule v " +
                     "WHERE v.nombre_place >= ? " +
                     "ORDER BY " +
                     "    ABS(v.nombre_place - ?) ASC, " +
                     "    CASE v.type_carburant " +
                     "        WHEN 'D' THEN 1 " +
                     "        WHEN 'ES' THEN 2 " +
                     "        WHEN 'H' THEN 3 " +
                     "        WHEN 'EL' THEN 4 " +
                     "        ELSE 5 " +
                     "    END ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, nombrePassager);
            ps.setInt(2, nombrePassager);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSetToVehicule(rs));
                }
            }
        }
        return vehicules;
    }

    /**
     * Récupère les véhicules disponibles pour une réservation
     * Ordonnés par règles d'assignation:
     * 1. Nombre de places le plus proche du nombre de passagers
     * 2. Priorité carburant: D > ES > H > EL
     * 3. Random si égalité (géré par RANDOM())
     * 
     * @param nombrePassager Nombre de passagers minimum requis
     * @param dateArrivee Date d'arrivée du vol
     * @param tempsAttenteMinutes Temps d'attente à l'aéroport en minutes
     * @param tempsTrajetMinutes Temps de trajet estimé en minutes
     * @return Liste des véhicules disponibles triés par pertinence
     */
    public List<Vehicule> findAvailableVehicles(int nombrePassager, Timestamp dateArrivee,
            int tempsAttenteMinutes, int tempsTrajetMinutes) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        
        // Calculer la fenêtre de temps pendant laquelle le véhicule sera occupé
        // Un véhicule est considéré comme disponible s'il n'a pas d'autre réservation
        // qui chevauche la période: [dateArrivee, dateArrivee + tempsAttente + tempsTrajet * 2]
        // (aller-retour estimé)
        
        String sql = "SELECT v.id, v.reference, v.nombre_place, v.type_carburant " +
                     "FROM vehicule v " +
                     "WHERE v.nombre_place >= ? " +
                     "AND v.id NOT IN (" +
                     "    SELECT r.id_vehicule FROM reservation r " +
                     "    WHERE r.id_vehicule IS NOT NULL " +
                     "    AND r.date_arrivee BETWEEN ? AND ? " +
                     ") " +
                     "ORDER BY " +
                     "    ABS(v.nombre_place - ?) ASC, " +  // Places les plus proches du besoin
                     "    CASE v.type_carburant " +
                     "        WHEN 'D' THEN 1 " +   // Diesel en priorité
                     "        WHEN 'ES' THEN 2 " +  // Essence
                     "        WHEN 'H' THEN 3 " +   // Hybride
                     "        WHEN 'EL' THEN 4 " +  // Électrique
                     "        ELSE 5 " +
                     "    END ASC, " +
                     "    RANDOM()";  // Random si égalité

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // Fenêtre de conflit: 2h avant et après l'heure d'arrivée (pour simplifier)
            long windowMs = 2 * 60 * 60 * 1000L; // 2 heures
            Timestamp windowStart = new Timestamp(dateArrivee.getTime() - windowMs);
            Timestamp windowEnd = new Timestamp(dateArrivee.getTime() + windowMs);

            ps.setInt(1, nombrePassager);
            ps.setTimestamp(2, windowStart);
            ps.setTimestamp(3, windowEnd);
            ps.setInt(4, nombrePassager);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSetToVehicule(rs));
                }
            }
        }
        return vehicules;
    }

    /**
     * Calcule le nombre de places restantes dans un véhicule pour une période donnée
     * en tenant compte des autres réservations qui partagent le même véhicule
     */
    public int getPlacesRestantes(int vehiculeId, Timestamp dateArrivee, int windowMinutes) throws SQLException {
        String sql = "SELECT v.nombre_place, COALESCE(SUM(r.nombre_passager), 0) as passagers_assignes " +
                     "FROM vehicule v " +
                     "LEFT JOIN reservation r ON r.id_vehicule = v.id " +
                     "    AND r.date_arrivee BETWEEN ? AND ? " +
                     "WHERE v.id = ? " +
                     "GROUP BY v.id, v.nombre_place";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            long windowMs = windowMinutes * 60 * 1000L;
            Timestamp windowStart = new Timestamp(dateArrivee.getTime() - windowMs);
            Timestamp windowEnd = new Timestamp(dateArrivee.getTime() + windowMs);

            ps.setTimestamp(1, windowStart);
            ps.setTimestamp(2, windowEnd);
            ps.setInt(3, vehiculeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int nombrePlace = rs.getInt("nombre_place");
                    int passagersAssignes = rs.getInt("passagers_assignes");
                    return nombrePlace - passagersAssignes;
                }
            }
        }
        return 0;
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
