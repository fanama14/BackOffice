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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            
            // Regrouper les réservations du même aéroport avec exactement la même date/heure d'arrivée
            List<List<Reservation>> groupes = new ArrayList<>();
            List<Reservation> remaining = new ArrayList<>(reservations);
            int groupeIdCounter = 1;
            
            while (!remaining.isEmpty()) {
                Reservation first = remaining.remove(0);
                List<Reservation> groupe = new ArrayList<>();
                groupe.add(first);
                
                Iterator<Reservation> it = remaining.iterator();
                while (it.hasNext()) {
                    Reservation r = it.next();
                    if (r.getAeroportId() == first.getAeroportId()
                            && r.getDateArrivee().getTime() == first.getDateArrivee().getTime()) {
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
            
            // === ASSIGNATION DES VÉHICULES PAR BIN PACKING ===
            // Pour chaque fenêtre temporelle, on assigne individuellement les réservations
            // aux véhicules disponibles (un véhicule peut embarquer plusieurs réservations
            // tant que le total de passagers <= nombre de places)
            List<VehiculeOccupation> occupations = new ArrayList<>();
            
            // Récupérer tous les véhicules, triés par capacité décroissante puis priorité carburant
            List<Vehicule> allVehicles = vehiculeDAO.findAll();
            allVehicles.sort((a, b) -> {
                if (a.getNombrePlace() != b.getNombrePlace()) {
                    return Integer.compare(b.getNombrePlace(), a.getNombrePlace());
                }
                return Integer.compare(getCarburantPriority(a.getTypeCarburant()),
                                       getCarburantPriority(b.getTypeCarburant()));
            });
            
            List<List<Reservation>> finalGroupes = new ArrayList<>();
            groupeIdCounter = 1;
            
            for (List<Reservation> windowGroup : groupes) {
                // Trier les réservations par nombre de passagers décroissant
                windowGroup.sort((a, b) -> Integer.compare(b.getNombrePassager(), a.getNombrePassager()));
                
                // Première arrivée de la fenêtre
                Timestamp firstArrival = windowGroup.get(0).getDateArrivee();
                for (Reservation r : windowGroup) {
                    if (r.getDateArrivee().before(firstArrival)) {
                        firstArrival = r.getDateArrivee();
                    }
                }
                
                // Heure de départ = première arrivée + temps d'attente
                long heureDepartMs = firstArrival.getTime() + (parametre.getTempsAttente() * 60 * 1000L);
                Timestamp heureDepart = new Timestamp(heureDepartMs);
                
                // Calculer les distances pour chaque réservation
                double maxDistanceKm = 0;
                for (Reservation r : windowGroup) {
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
                
                // Estimation conservatrice du retour (distance max de la fenêtre)
                int tempsTrajetMax = parametre.calculerTempsTrajet(maxDistanceKm);
                long heureRetourEstimeeMs = heureDepartMs + (tempsTrajetMax * 60 * 1000L * 2);
                Timestamp heureRetourEstimee = new Timestamp(heureRetourEstimeeMs);
                
                // Capacité restante par véhicule disponible pour cette fenêtre
                LinkedHashMap<Integer, Integer> remainingCapacity = new LinkedHashMap<>();
                LinkedHashMap<Integer, Vehicule> vehiculeById = new LinkedHashMap<>();
                LinkedHashMap<Integer, List<Reservation>> vehiculeResaMap = new LinkedHashMap<>();
                
                for (Vehicule v : allVehicles) {
                    if (!isVehiculeOccupe(occupations, v.getId(), heureDepart, heureRetourEstimee)) {
                        remainingCapacity.put(v.getId(), v.getNombrePlace());
                        vehiculeById.put(v.getId(), v);
                    }
                }
                
                List<Reservation> unassigned = new ArrayList<>();
                
                // First-fit decreasing bin packing
                for (Reservation r : windowGroup) {
                    boolean assigned = false;
                    for (Map.Entry<Integer, Integer> entry : remainingCapacity.entrySet()) {
                        int vId = entry.getKey();
                        int capaciteRestante = entry.getValue();
                        if (capaciteRestante >= r.getNombrePassager()) {
                            if (!vehiculeResaMap.containsKey(vId)) {
                                vehiculeResaMap.put(vId, new ArrayList<>());
                            }
                            vehiculeResaMap.get(vId).add(r);
                            remainingCapacity.put(vId, capaciteRestante - r.getNombrePassager());
                            assigned = true;
                            break;
                        }
                    }
                    if (!assigned) {
                        unassigned.add(r);
                    }
                }
                
                // Créer les groupes par véhicule
                for (Map.Entry<Integer, List<Reservation>> entry : vehiculeResaMap.entrySet()) {
                    int vId = entry.getKey();
                    Vehicule v = vehiculeById.get(vId);
                    List<Reservation> resasInVehicle = entry.getValue();
                    
                    // Calcul du retour basé sur la distance max de CE véhicule
                    double vMaxDist = 0;
                    for (Reservation r : resasInVehicle) {
                        if (r.getDistanceKm() > vMaxDist) {
                            vMaxDist = r.getDistanceKm();
                        }
                    }
                    int vTempsTrajet = parametre.calculerTempsTrajet(vMaxDist);
                    long vHeureRetourMs = heureDepartMs + (vTempsTrajet * 60 * 1000L * 2);
                    Timestamp vHeureRetour = new Timestamp(vHeureRetourMs);
                    
                    int ordre = 1;
                    for (Reservation r : resasInVehicle) {
                        r.setGroupeId(groupeIdCounter);
                        r.setOrdreLivraison(ordre++);
                        r.setHeureDepartAeroport(heureDepart);
                        r.setHeureRetourAeroport(vHeureRetour);
                        r.setVehiculeReference(v.getReference());
                        r.setVehiculeTypeCarburant(v.getTypeCarburantLibelle());
                        r.setVehiculeNombrePlace(v.getNombrePlace());
                    }
                    
                    occupations.add(new VehiculeOccupation(vId, heureDepart, vHeureRetour));
                    finalGroupes.add(resasInVehicle);
                    groupeIdCounter++;
                }
                
                // Groupe des réservations sans véhicule
                if (!unassigned.isEmpty()) {
                    int ordre = 1;
                    for (Reservation r : unassigned) {
                        r.setGroupeId(groupeIdCounter);
                        r.setOrdreLivraison(ordre++);
                        r.setHeureDepartAeroport(heureDepart);
                        r.setHeureRetourAeroport(null);
                    }
                    finalGroupes.add(unassigned);
                    groupeIdCounter++;
                }
            }
            
            // Reconstituer la liste aplatie
            List<Reservation> result = new ArrayList<>();
            for (List<Reservation> groupe : finalGroupes) {
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
     * Retourne la priorité du carburant (plus petit = meilleur)
     */
    private int getCarburantPriority(String typeCarburant) {
        if (typeCarburant == null) return 5;
        switch (typeCarburant) {
            case "D": return 1;
            case "ES": return 2;
            case "H": return 3;
            case "EL": return 4;
            default: return 5;
        }
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
