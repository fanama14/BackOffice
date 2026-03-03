package mg.teste;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.annotations.POST;
import mg.framework.annotations.RequestParam;
import mg.framework.ModelView;

import com.backoffice.dao.HotelDAO;
import com.backoffice.dao.ParametreDAO;
import com.backoffice.dao.ReservationDAO;
import com.backoffice.dao.VehiculeDAO;
import com.backoffice.model.Hotel;
import com.backoffice.model.Parametre;
import com.backoffice.model.Reservation;
import com.backoffice.model.Vehicule;

import java.sql.Timestamp;
import java.util.List;

@Controller
public class PlanificationController {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();
    private final HotelDAO hotelDAO = new HotelDAO();
    private final ParametreDAO parametreDAO = new ParametreDAO();

    /**
     * Affiche la page de planification avec la liste des réservations
     */
    @GET("planification")
    public ModelView showPlanification(
            @RequestParam(value = "dateDebut", required = false) String dateDebut,
            @RequestParam(value = "dateFin", required = false) String dateFin) {
        
        ModelView mv = new ModelView("planification");
        
        try {
            Parametre parametre = parametreDAO.getParametres();
            mv.addData("parametre", parametre);
            
            // Dates par défaut : aujourd'hui et dans 7 jours
            Timestamp tsDebut;
            Timestamp tsFin;
            
            if (dateDebut != null && !dateDebut.isEmpty()) {
                tsDebut = Timestamp.valueOf(dateDebut + " 00:00:00");
            } else {
                // Date du jour
                tsDebut = new Timestamp(System.currentTimeMillis());
                tsDebut = Timestamp.valueOf(tsDebut.toString().substring(0, 10) + " 00:00:00");
            }
            
            if (dateFin != null && !dateFin.isEmpty()) {
                tsFin = Timestamp.valueOf(dateFin + " 23:59:59");
            } else {
                // Dans 7 jours
                tsFin = new Timestamp(tsDebut.getTime() + 7L * 24 * 60 * 60 * 1000);
                tsFin = Timestamp.valueOf(tsFin.toString().substring(0, 10) + " 23:59:59");
            }
            
            List<Reservation> reservations = reservationDAO.findForPlanification(tsDebut, tsFin, parametre);
            mv.addData("reservations", reservations);
            mv.addData("dateDebut", tsDebut.toString().substring(0, 10));
            mv.addData("dateFin", tsFin.toString().substring(0, 10));
            
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement des réservations : " + e.getMessage());
            e.printStackTrace();
        }
        
        return mv;
    }

    /**
     * Assigne automatiquement un véhicule à une réservation
     * Règles d'assignation :
     * 1. Véhicule disponible (pas déjà en course)
     * 2. Nombre de places >= nombre de passagers
     * 3. Si plusieurs véhicules disponibles : choisir celui avec le nombre de places le plus proche
     * 4. Si égalité : priorité Diesel > Essence > Hybride > Électrique
     * 5. Si encore égalité : random
     */
    @POST("planification/assigner")
    public ModelView assignerVehicule(
            @RequestParam("reservationId") int reservationId,
            @RequestParam(value = "dateDebut", required = false) String dateDebut,
            @RequestParam(value = "dateFin", required = false) String dateFin) {
        
        ModelView mv = new ModelView("planification");
        
        try {
            Parametre parametre = parametreDAO.getParametres();
            Reservation reservation = reservationDAO.findById(reservationId);
            
            if (reservation == null) {
                mv.addData("error", "Réservation non trouvée");
                return reloadPlanification(mv, dateDebut, dateFin, parametre);
            }
            
            // Récupérer l'hôtel pour calculer le temps de trajet
            Hotel hotel = hotelDAO.findById(reservation.getHotelId());
            int tempsTrajetMinutes = parametre.calculerTempsTrajet(hotel.getDistanceAeroport());
            
            // Rechercher les véhicules disponibles triés par pertinence
            List<Vehicule> vehiculesDisponibles = vehiculeDAO.findAvailableVehicles(
                reservation.getNombrePassager(),
                reservation.getDateArrivee(),
                parametre.getTempsAttente(),
                tempsTrajetMinutes
            );
            
            if (vehiculesDisponibles.isEmpty()) {
                mv.addData("error", "Aucun véhicule disponible pour cette réservation (passagers: " 
                    + reservation.getNombrePassager() + ")");
                return reloadPlanification(mv, dateDebut, dateFin, parametre);
            }
            
            // Prendre le premier véhicule (déjà trié par les règles d'assignation)
            Vehicule vehiculeChoisi = vehiculesDisponibles.get(0);
            
            // Vérifier qu'il y a assez de places en tenant compte du partage
            int placesRestantes = vehiculeDAO.getPlacesRestantes(
                vehiculeChoisi.getId(),
                reservation.getDateArrivee(),
                120  // fenêtre de 2h
            );
            
            // Si pas assez de places avec ce véhicule, chercher le suivant
            if (placesRestantes < reservation.getNombrePassager()) {
                boolean vehiculeTrouve = false;
                for (Vehicule v : vehiculesDisponibles) {
                    placesRestantes = vehiculeDAO.getPlacesRestantes(
                        v.getId(),
                        reservation.getDateArrivee(),
                        120
                    );
                    if (placesRestantes >= reservation.getNombrePassager()) {
                        vehiculeChoisi = v;
                        vehiculeTrouve = true;
                        break;
                    }
                }
                
                if (!vehiculeTrouve) {
                    mv.addData("error", "Aucun véhicule avec assez de places disponibles");
                    return reloadPlanification(mv, dateDebut, dateFin, parametre);
                }
            }
            
            // Assigner le véhicule
            reservationDAO.assignerVehicule(reservationId, vehiculeChoisi.getId());
            mv.addData("success", "Véhicule " + vehiculeChoisi.getReference() + " assigné avec succès (" 
                + vehiculeChoisi.getNombrePlace() + " places, " + vehiculeChoisi.getTypeCarburantLibelle() + ")");
            
            return reloadPlanification(mv, dateDebut, dateFin, parametre);
            
        } catch (Exception e) {
            mv.addData("error", "Erreur lors de l'assignation : " + e.getMessage());
            e.printStackTrace();
            try {
                return reloadPlanification(mv, dateDebut, dateFin, parametreDAO.getParametres());
            } catch (Exception ex) {
                return mv;
            }
        }
    }

    /**
     * Retire l'assignation d'un véhicule
     */
    @POST("planification/retirer")
    public ModelView retirerVehicule(
            @RequestParam("reservationId") int reservationId,
            @RequestParam(value = "dateDebut", required = false) String dateDebut,
            @RequestParam(value = "dateFin", required = false) String dateFin) {
        
        ModelView mv = new ModelView("planification");
        
        try {
            reservationDAO.retirerVehicule(reservationId);
            mv.addData("success", "Véhicule retiré de la réservation");
            
            Parametre parametre = parametreDAO.getParametres();
            return reloadPlanification(mv, dateDebut, dateFin, parametre);
            
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du retrait : " + e.getMessage());
            try {
                return reloadPlanification(mv, dateDebut, dateFin, parametreDAO.getParametres());
            } catch (Exception ex) {
                return mv;
            }
        }
    }

    /**
     * Recharge les données de planification dans le ModelView
     */
    private ModelView reloadPlanification(ModelView mv, String dateDebut, String dateFin, Parametre parametre) 
            throws Exception {
        
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
        
        List<Reservation> reservations = reservationDAO.findForPlanification(tsDebut, tsFin, parametre);
        mv.addData("reservations", reservations);
        mv.addData("dateDebut", tsDebut.toString().substring(0, 10));
        mv.addData("dateFin", tsFin.toString().substring(0, 10));
        
        return mv;
    }
}
