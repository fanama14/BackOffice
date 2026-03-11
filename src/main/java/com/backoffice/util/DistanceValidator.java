package com.backoffice.util;

import com.backoffice.dao.DistanceDAO;
import com.backoffice.dao.LieuxDAO;
import com.backoffice.model.Distance;
import com.backoffice.model.Lieux;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilitaire pour vérifier et gérer les distances entre tous les lieux.
 * Selon les règles métier, toutes les distances possibles doivent être
 * présentes
 * dans la base de données pour permettre le regroupement optimal des
 * réservations.
 */
public class DistanceValidator {

    private final DistanceDAO distanceDAO;
    private final LieuxDAO lieuxDAO;

    public DistanceValidator(DistanceDAO distanceDAO, LieuxDAO lieuxDAO) {
        this.distanceDAO = distanceDAO;
        this.lieuxDAO = lieuxDAO;
    }

    /**
     * Vérifie que toutes les combinaisons de distances existent
     * 
     * @return Liste des paires de lieux manquantes
     */
    public List<MissingDistance> findMissingDistances() throws SQLException {
        List<MissingDistance> missing = new ArrayList<>();
        List<Lieux> allLieux = lieuxDAO.findAll();

        // Vérifier toutes les paires de lieux
        for (int i = 0; i < allLieux.size(); i++) {
            for (int j = i + 1; j < allLieux.size(); j++) {
                Lieux lieu1 = allLieux.get(i);
                Lieux lieu2 = allLieux.get(j);

                // Vérifier si une distance existe entre ces deux lieux
                Distance distance = distanceDAO.findByLieux(lieu1.getId(), lieu2.getId());

                if (distance == null) {
                    missing.add(new MissingDistance(lieu1, lieu2));
                }
            }
        }

        return missing;
    }

    /**
     * Génère un rapport des distances manquantes
     */
    public String generateMissingDistancesReport() throws SQLException {
        List<MissingDistance> missing = findMissingDistances();

        if (missing.isEmpty()) {
            return "✓ Toutes les distances nécessaires sont présentes dans la base de données.";
        }

        StringBuilder report = new StringBuilder();
        report.append("⚠ Distances manquantes : ").append(missing.size()).append("\n\n");
        report.append("Les distances suivantes doivent être ajoutées à la base de données:\n\n");

        for (MissingDistance md : missing) {
            report.append(String.format("- Distance entre '%s' (ID: %d) et '%s' (ID: %d)\n",
                    md.lieu1.getLieu(), md.lieu1.getId(),
                    md.lieu2.getLieu(), md.lieu2.getId()));
        }

        report.append("\nScript SQL suggéré:\n\n");
        for (MissingDistance md : missing) {
            report.append(String.format(
                    "INSERT INTO distance (lieux_from, lieux_to, valeur) VALUES (%d, %d, 0.0); -- %s <-> %s\n",
                    md.lieu1.getId(), md.lieu2.getId(),
                    md.lieu1.getLieu(), md.lieu2.getLieu()));
        }

        return report.toString();
    }

    /**
     * Crée automatiquement les distances manquantes avec valeur par défaut
     * 
     * @param defaultValue Valeur par défaut à utiliser (en km)
     */
    public void createMissingDistances(double defaultValue) throws SQLException {
        List<MissingDistance> missing = findMissingDistances();

        for (MissingDistance md : missing) {
            Distance distance = new Distance();
            distance.setLieuxFrom(md.lieu1.getId());
            distance.setLieuxTo(md.lieu2.getId());
            distance.setValeur(defaultValue);

            distanceDAO.insert(distance);
        }
    }

    /**
     * Classe pour représenter une distance manquante
     */
    public static class MissingDistance {
        private final Lieux lieu1;
        private final Lieux lieu2;

        public MissingDistance(Lieux lieu1, Lieux lieu2) {
            this.lieu1 = lieu1;
            this.lieu2 = lieu2;
        }

        public Lieux getLieu1() {
            return lieu1;
        }

        public Lieux getLieu2() {
            return lieu2;
        }
    }
}
