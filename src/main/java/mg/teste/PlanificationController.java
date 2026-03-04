package mg.teste;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.annotations.RequestParam;
import mg.framework.ModelView;

import com.backoffice.dao.DistanceDAO;
import com.backoffice.dao.HotelDAO;
import com.backoffice.dao.ParametreDAO;
import com.backoffice.dao.ReservationDAO;
import com.backoffice.dao.VehiculeDAO;
import com.backoffice.dao.AeroportDAO;
import com.backoffice.model.Aeroport;
import com.backoffice.model.Hotel;
import com.backoffice.model.Parametre;
import com.backoffice.model.Reservation;
import com.backoffice.model.ReservationGroup;
import com.backoffice.model.Vehicule;
import com.backoffice.service.GroupingService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PlanificationController {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final ParametreDAO parametreDAO = new ParametreDAO();
    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private final HotelDAO hotelDAO = new HotelDAO();
    private final DistanceDAO distanceDAO = new DistanceDAO();
    private final AeroportDAO aeroportDAO = new AeroportDAO();

    /**
     * Affiche la page de planification/simulation avec regroupement automatique.
     * Règles de regroupement:
     * - Les réservations avec même date/heure d'arrivée peuvent être groupées
     * - Total passagers <= capacité du véhicule
     * - Ordre de visite: distance la plus courte d'abord, puis alphabétique
     */
    @GET("planification")
    public ModelView showPlanification(
            @RequestParam(value = "dateDebut", required = false) String dateDebut,
            @RequestParam(value = "dateFin", required = false) String dateFin) {

        ModelView mv = new ModelView("planification");

        try {
            Parametre parametre = parametreDAO.getParametres();
            mv.addData("parametre", parametre);

            Timestamp tsDebut;
            Timestamp tsFin;

            if (dateDebut != null && !dateDebut.isEmpty()) {
                tsDebut = Timestamp.valueOf(dateDebut + " 00:00:00");
            } else {
                tsDebut = new Timestamp(System.currentTimeMillis());
                tsDebut = Timestamp.valueOf(tsDebut.toString().substring(0, 10) + " 00:00:00");
            }

            if (dateFin != null && !dateFin.isEmpty()) {
                tsFin = Timestamp.valueOf(dateFin + " 23:59:59");
            } else {
                tsFin = new Timestamp(tsDebut.getTime() + 7L * 24 * 60 * 60 * 1000);
                tsFin = Timestamp.valueOf(tsFin.toString().substring(0, 10) + " 23:59:59");
            }

            // Récupérer les réservations de la période
            List<Reservation> reservations = reservationDAO.findByPeriode(tsDebut, tsFin);

            // Utiliser le service de regroupement pour optimiser l'assignation des véhicules
            GroupingService groupingService = new GroupingService(
                vehiculeDAO, hotelDAO, aeroportDAO, distanceDAO
            );

            List<ReservationGroup> groups = groupingService.groupReservations(reservations, parametre);

            // Compiler toutes les réservations des groupes pour l'affichage
            List<Reservation> reservationsWithVehicles = new ArrayList<>();
            for (ReservationGroup group : groups) {
                reservationsWithVehicles.addAll(group.getReservations());
            }

            mv.addData("reservations", reservationsWithVehicles);
            mv.addData("groups", groups);
            mv.addData("dateDebut", tsDebut.toString().substring(0, 10));
            mv.addData("dateFin", tsFin.toString().substring(0, 10));
            mv.addData("grouped", true);

        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement : " + e.getMessage());
            e.printStackTrace();
        }

        return mv;
    }

    /**
     * Vérifie si un véhicule est déjà occupé pendant une fenêtre de temps
     */
    private boolean isVehiculeOccupe(List<VehiculeOccupation> occupations, int vehiculeId,
            Timestamp debut, Timestamp fin) {
        for (VehiculeOccupation occ : occupations) {
            if (occ.vehiculeId == vehiculeId) {
                // Vérifier le chevauchement
                if (debut.before(occ.fin) && fin.after(occ.debut)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Affiche la planification avec regroupement de réservations.
     * Règles de regroupement:
     * 1. Les réservations avec même date/heure d'arrivée peuvent être groupées
     * 2. Total passagers < capacité du véhicule (pas égal)
     * 3. Ordre de visite: distance la plus courte d'abord
     * 4. Si distances égales: ordre alphabétique
     */
    @GET("planification-grouped")
    public ModelView showPlanificationWithGrouping(
            @RequestParam(value = "dateDebut", required = false) String dateDebut,
            @RequestParam(value = "dateFin", required = false) String dateFin) {

        ModelView mv = new ModelView("planification");

        try {
            Parametre parametre = parametreDAO.getParametres();
            mv.addData("parametre", parametre);

            Timestamp tsDebut;
            Timestamp tsFin;

            if (dateDebut != null && !dateDebut.isEmpty()) {
                tsDebut = Timestamp.valueOf(dateDebut + " 00:00:00");
            } else {
                tsDebut = new Timestamp(System.currentTimeMillis());
                tsDebut = Timestamp.valueOf(tsDebut.toString().substring(0, 10) + " 00:00:00");
            }

            if (dateFin != null && !dateFin.isEmpty()) {
                tsFin = Timestamp.valueOf(dateFin + " 23:59:59");
            } else {
                tsFin = new Timestamp(tsDebut.getTime() + 7L * 24 * 60 * 60 * 1000);
                tsFin = Timestamp.valueOf(tsFin.toString().substring(0, 10) + " 23:59:59");
            }

            // Récupérer les réservations de la période
            List<Reservation> reservations = reservationDAO.findByPeriode(tsDebut, tsFin);

            // Utiliser le service de regroupement
            GroupingService groupingService = new GroupingService(
                    vehiculeDAO, hotelDAO, aeroportDAO, distanceDAO);

            List<ReservationGroup> groups = groupingService.groupReservations(reservations, parametre);

            // Compiler toutes les réservations des groupes pour l'affichage
            List<Reservation> reservationsWithVehicles = new ArrayList<>();
            for (ReservationGroup group : groups) {
                reservationsWithVehicles.addAll(group.getReservations());
            }

            mv.addData("reservations", reservationsWithVehicles);
            mv.addData("groups", groups);
            mv.addData("dateDebut", tsDebut.toString().substring(0, 10));
            mv.addData("dateFin", tsFin.toString().substring(0, 10));
            mv.addData("grouped", true);

        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement : " + e.getMessage());
            e.printStackTrace();
        }

        return mv;
    }

    /**
     * Classe interne pour tracker l'occupation d'un véhicule
     */
    private static class VehiculeOccupation {
        int vehiculeId;
        Timestamp debut;
        Timestamp fin;

        VehiculeOccupation(int vehiculeId, Timestamp debut, Timestamp fin) {
            this.vehiculeId = vehiculeId;
            this.debut = debut;
            this.fin = fin;
        }
    }
}
