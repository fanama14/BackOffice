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
import java.util.Iterator;
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
            
            // === REGROUPEMENT ===
            // Trier par aéroport puis par date d'arrivée
            reservations.sort((a, b) -> {
                if (a.getAeroportId() != b.getAeroportId()) {
                    return Integer.compare(a.getAeroportId(), b.getAeroportId());
                }
                return a.getDateArrivee().compareTo(b.getDateArrivee());
            });
            
            // Regrouper les réservations du même aéroport dans la fenêtre temps_attente
            List<List<Reservation>> groupes = new ArrayList<>();
            List<Reservation> remaining = new ArrayList<>(reservations);
            int groupeIdCounter = 1;
            
            while (!remaining.isEmpty()) {
                Reservation first = remaining.remove(0);
                List<Reservation> groupe = new ArrayList<>();
                groupe.add(first);
                
                long windowEndMs = first.getDateArrivee().getTime() + (parametre.getTempsAttente() * 60 * 1000L);
                
                Iterator<Reservation> it = remaining.iterator();
                while (it.hasNext()) {
                    Reservation r = it.next();
                    if (r.getAeroportId() == first.getAeroportId()
                            && r.getDateArrivee().getTime() <= windowEndMs) {
                        groupe.add(r);
                        it.remove();
                    }
                }
                
                // Trier le groupe par nombre de passagers décroissant (les plus gros en premier)
                groupe.sort((a, b) -> Integer.compare(b.getNombrePassager(), a.getNombrePassager()));
                
                // Assigner groupeId et ordreLivraison
                int ordre = 1;
                for (Reservation r : groupe) {
                    r.setGroupeId(groupeIdCounter);
                    r.setOrdreLivraison(ordre++);
                }
                
                groupes.add(groupe);
                groupeIdCounter++;
            }
            
            // === ASSIGNATION DES VÉHICULES PAR GROUPE ===
            List<VehiculeOccupation> occupations = new ArrayList<>();
            
            for (List<Reservation> groupe : groupes) {
                // Total passagers du groupe
                int totalPassagers = 0;
                for (Reservation r : groupe) {
                    totalPassagers += r.getNombrePassager();
                }
                
                // Première arrivée du groupe
                Timestamp firstArrival = groupe.get(groupe.size() - 1).getDateArrivee();
                for (Reservation r : groupe) {
                    if (r.getDateArrivee().before(firstArrival)) {
                        firstArrival = r.getDateArrivee();
                    }
                }
                
                // Heure de départ = première arrivée + temps d'attente
                long heureDepartMs = firstArrival.getTime() + (parametre.getTempsAttente() * 60 * 1000L);
                Timestamp heureDepart = new Timestamp(heureDepartMs);
                
                // Calculer les distances pour chaque réservation du groupe
                double maxDistanceKm = 0;
                for (Reservation r : groupe) {
                    Hotel hotel = hotelDAO.findById(r.getHotelId());
                    if (hotel == null) continue;
                    
                    Aeroport aeroport = aeroportDAO.findById(r.getAeroportId());
                    double distanceKm = 0;
                    if (aeroport != null) {
                        distanceKm = distanceDAO.getDistanceKm(aeroport.getLieuxId(), hotel.getLieuxId());
                    }
                    r.setDistanceKm(distanceKm);
                    if (distanceKm > maxDistanceKm) {
                        maxDistanceKm = distanceKm;
                    }
                }
                
                // Heure de retour = départ + aller-retour vers l'hôtel le plus éloigné
                int tempsTrajetMinutes = parametre.calculerTempsTrajet(maxDistanceKm);
                long heureRetourMs = heureDepartMs + (tempsTrajetMinutes * 60 * 1000L * 2);
                Timestamp heureRetour = new Timestamp(heureRetourMs);
                
                // Trouver le meilleur véhicule pour le TOTAL des passagers du groupe
                List<Vehicule> candidats = vehiculeDAO.findBestVehicles(totalPassagers);
                
                Vehicule vehiculeChoisi = null;
                for (Vehicule v : candidats) {
                    if (!isVehiculeOccupe(occupations, v.getId(), heureDepart, heureRetour)) {
                        vehiculeChoisi = v;
                        break;
                    }
                }
                
                // Assigner le même véhicule et les mêmes horaires à tout le groupe
                for (Reservation r : groupe) {
                    r.setHeureDepartAeroport(heureDepart);
                    r.setHeureRetourAeroport(heureRetour);
                    
                    if (vehiculeChoisi != null) {
                        r.setVehiculeReference(vehiculeChoisi.getReference());
                        r.setVehiculeTypeCarburant(vehiculeChoisi.getTypeCarburantLibelle());
                        r.setVehiculeNombrePlace(vehiculeChoisi.getNombrePlace());
                    }
                }
                
                if (vehiculeChoisi != null) {
                    occupations.add(new VehiculeOccupation(vehiculeChoisi.getId(), heureDepart, heureRetour));
                }
            }
            
            // Reconstituer la liste aplatie (groupes dans l'ordre, au sein de chaque groupe par ordreLivraison)
            List<Reservation> result = new ArrayList<>();
            for (List<Reservation> groupe : groupes) {
                result.addAll(groupe);
            }
            
            mv.addData("reservations", result);
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
