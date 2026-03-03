package com.backoffice.dao;

import com.backoffice.database.DatabaseConnection;
import com.backoffice.model.Parametre;

import java.sql.*;

public class ParametreDAO {

    /**
     * Récupère les paramètres actuels (il n'y a qu'une seule ligne)
     */
    public Parametre getParametres() throws SQLException {
        String sql = "SELECT id, temps_attente, vitesse_moyenne FROM parametre LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                Parametre p = new Parametre();
                p.setId(rs.getInt("id"));
                p.setTempsAttente(rs.getInt("temps_attente"));
                p.setVitesseMoyenne(rs.getInt("vitesse_moyenne"));
                return p;
            }
        }
        // Retourne des valeurs par défaut si aucun paramètre n'existe
        return new Parametre(0, 30, 30);
    }

    /**
     * Met à jour les paramètres
     */
    public void update(Parametre parametre) throws SQLException {
        String sql = "UPDATE parametre SET temps_attente = ?, vitesse_moyenne = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, parametre.getTempsAttente());
            ps.setInt(2, parametre.getVitesseMoyenne());
            ps.setInt(3, parametre.getId());
            ps.executeUpdate();
        }
    }
}
