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
import com.backoffice.model.Vehicule;

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
     * Affiche la page de planification/simulation.
     * Pour chaque réservation dans la période :
     * - Assigne le meilleur véhicule (places >= passagers, le plus proche, puis D > ES > H > EL)
     * - Calcule heure départ aéroport = date_arrivee + temps_attente
     * - Calcule heure retour aéroport = heure_depart + temps_trajet_aller_retour
     *   (temps_trajet = distance(aéroport_lieux, hotel_lieux) / vitesse_moyenne)
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
            
            // Simuler l'assignation des véhicules
            // On garde trace des véhicules déjà utilisés avec leur fenêtre d'occupation
            List<VehiculeOccupation> occupations = new ArrayList<>();
            
            for (Reservation r : reservations) {
                // Récupérer l'hôtel pour connaître son lieux_id
                Hotel hotel = hotelDAO.findById(r.getHotelId());
                if (hotel == null) continue;
                
                // Récupérer l'aéroport de la réservation pour connaître son lieux_id
                Aeroport aeroport = aeroportDAO.findById(r.getAeroportId());
                double distanceKm = 0;
                if (aeroport != null) {
                    distanceKm = distanceDAO.getDistanceKm(aeroport.getLieuxId(), hotel.getLieuxId());
                }
                
                r.setDistanceKm(distanceKm);
                
                // Calculer les temps
                int tempsTrajetMinutes = parametre.calculerTempsTrajet(distanceKm);
                
                // Heure de départ aéroport = date arrivée + temps d'attente
                long heureDepartMs = r.getDateArrivee().getTime() + (parametre.getTempsAttente() * 60 * 1000L);
                Timestamp heureDepart = new Timestamp(heureDepartMs);
                r.setHeureDepartAeroport(heureDepart);
                
                // Heure de retour aéroport = heure départ + temps trajet aller-retour
                long heureRetourMs = heureDepartMs + (tempsTrajetMinutes * 60 * 1000L * 2);
                Timestamp heureRetour = new Timestamp(heureRetourMs);
                r.setHeureRetourAeroport(heureRetour);
                
                // Trouver le meilleur véhicule disponible
                List<Vehicule> candidats = vehiculeDAO.findBestVehicles(r.getNombrePassager());
                
                Vehicule vehiculeChoisi = null;
                for (Vehicule v : candidats) {
                    // Vérifier que ce véhicule n'est pas occupé pendant la fenêtre [heureDepart, heureRetour]
                    if (!isVehiculeOccupe(occupations, v.getId(), heureDepart, heureRetour)) {
                        vehiculeChoisi = v;
                        break;
                    }
                }
                
                if (vehiculeChoisi != null) {
                    r.setVehiculeReference(vehiculeChoisi.getReference());
                    r.setVehiculeTypeCarburant(vehiculeChoisi.getTypeCarburantLibelle());
                    r.setVehiculeNombrePlace(vehiculeChoisi.getNombrePlace());
                    
                    // Marquer le véhicule comme occupé pendant cette fenêtre
                    occupations.add(new VehiculeOccupation(vehiculeChoisi.getId(), heureDepart, heureRetour));
                }
            }
            
            mv.addData("reservations", reservations);
            mv.addData("dateDebut", tsDebut.toString().substring(0, 10));
            mv.addData("dateFin", tsFin.toString().substring(0, 10));
            
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
