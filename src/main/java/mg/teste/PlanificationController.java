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
            
            // === REGROUPEMENT PAR FENÊTRE DE TEMPS D'ATTENTE GLISSANTE (Sprint 5) ===
            // Règle : on prend la première réservation (par heure d'arrivée) comme ancre.
            // On ouvre une fenêtre [ancre.dateArrivee, ancre.dateArrivee + tempsAttente].
            // Toutes les réservations dont l'heure d'arrivée tombe dans cette fenêtre sont groupées.
            // La fenêtre suivante commence à la prochaine réservation APRÈS la fin de la fenêtre.
            //
            // Exemple avec tempsAttente = 30 min :
            //   resa 1: 08:00, resa 2: 08:18, resa 3: 08:40, resa 4: 09:00
            //   Fenêtre 1: [08:00, 08:30] → resa 1 + resa 2
            //   Fenêtre 2: [08:40, 09:10] → resa 3 + resa 4

            // D'abord, grouper par aéroport puis par jour
            Map<String, List<Reservation>> byAeroportAndDay = new LinkedHashMap<>();
            for (Reservation r : reservations) {
                String key = r.getAeroportId() + "_" + String.format("%tF", r.getDateArrivee());
                byAeroportAndDay.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
            }

            List<List<Reservation>> groupes = new ArrayList<>();
            int groupeIdCounter = 1;

            for (Map.Entry<String, List<Reservation>> dayEntry : byAeroportAndDay.entrySet()) {
                List<Reservation> dayReservations = dayEntry.getValue();
                // Trier par heure d'arrivée
                dayReservations.sort((a, b) -> a.getDateArrivee().compareTo(b.getDateArrivee()));

                int i = 0;
                while (i < dayReservations.size()) {
                    // L'ancre est la première réservation non encore assignée
                    Reservation anchor = dayReservations.get(i);
                    long anchorTime = anchor.getDateArrivee().getTime();
                    long windowEnd = anchorTime + (parametre.getTempsAttente() * 60 * 1000L);

                    List<Reservation> groupe = new ArrayList<>();
                    groupe.add(anchor);

                    int j = i + 1;
                    while (j < dayReservations.size()) {
                        Reservation candidate = dayReservations.get(j);
                        if (candidate.getDateArrivee().getTime() <= windowEnd) {
                            groupe.add(candidate);
                            j++;
                        } else {
                            break;
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
                    i = j; // Avancer à la prochaine réservation hors fenêtre
                }
            }
            
            // === ASSIGNATION DES VÉHICULES PAR BEST-FIT (Sprint 5) ===
            // Pour chaque fenêtre temporelle, on calcule le total de passagers du groupe
            // et on assigne le véhicule dont la capacité est la plus proche (best-fit)
            // Priorité carburant en cas d'égalité : D > ES > H > EL
            List<VehiculeOccupation> occupations = new ArrayList<>();
            
            // Récupérer tous les véhicules
            List<Vehicule> allVehicles = vehiculeDAO.findAll();
            
            List<List<Reservation>> finalGroupes = new ArrayList<>();
            groupeIdCounter = 1;
            
            for (List<Reservation> windowGroup : groupes) {
                // Calculer le total de passagers pour ce groupe
                int totalPassagers = 0;
                for (Reservation r : windowGroup) {
                    totalPassagers += r.getNombrePassager();
                }
                
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
                
                // Calculer les distances et charger le nom d'hôtel pour chaque réservation
                double maxDistanceKm = 0;
                for (Reservation r : windowGroup) {
                    Hotel hotel = hotelDAO.findById(r.getHotelId());
                    if (hotel == null) continue;
                    
                    r.setHotelNom(hotel.getNom());
                    
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
                
                // Trier par distance croissante, puis alphabétique par nom d'hôtel si même distance
                windowGroup.sort((a, b) -> {
                    int distCmp = Double.compare(a.getDistanceKm(), b.getDistanceKm());
                    if (distCmp != 0) return distCmp;
                    String nomA = a.getHotelNom() != null ? a.getHotelNom() : "";
                    String nomB = b.getHotelNom() != null ? b.getHotelNom() : "";
                    return nomA.compareTo(nomB);
                });
                
                // Calcul du temps de retour
                int tempsTrajetMax = parametre.calculerTempsTrajet(maxDistanceKm);
                long heureRetourMs = heureDepartMs + (tempsTrajetMax * 60 * 1000L * 2);
                Timestamp heureRetour = new Timestamp(heureRetourMs);
                
                // Trouver le meilleur véhicule (best-fit) :
                // capacité >= totalPassagers, le plus proche en capacité, puis priorité carburant
                final int nbPassagers = totalPassagers;
                List<Vehicule> candidats = new ArrayList<>();
                for (Vehicule v : allVehicles) {
                    if (v.getNombrePlace() >= nbPassagers 
                            && !isVehiculeOccupe(occupations, v.getId(), heureDepart, heureRetour)) {
                        candidats.add(v);
                    }
                }
                // Trier par best-fit : capacité la plus proche du total, puis priorité carburant
                candidats.sort((a, b) -> {
                    int diffA = a.getNombrePlace() - nbPassagers;
                    int diffB = b.getNombrePlace() - nbPassagers;
                    if (diffA != diffB) {
                        return Integer.compare(diffA, diffB); // plus petit écart d'abord
                    }
                    return Integer.compare(getCarburantPriority(a.getTypeCarburant()),
                                           getCarburantPriority(b.getTypeCarburant()));
                });
                
                if (!candidats.isEmpty()) {
                    Vehicule bestVehicule = candidats.get(0);
                    
                    int ordre = 1;
                    for (Reservation r : windowGroup) {
                        r.setGroupeId(groupeIdCounter);
                        r.setOrdreLivraison(ordre++);
                        r.setHeureDepartAeroport(heureDepart);
                        r.setHeureRetourAeroport(heureRetour);
                        r.setVehiculeReference(bestVehicule.getReference());
                        r.setVehiculeTypeCarburant(bestVehicule.getTypeCarburantLibelle());
                        r.setVehiculeNombrePlace(bestVehicule.getNombrePlace());
                    }
                    
                    occupations.add(new VehiculeOccupation(bestVehicule.getId(), heureDepart, heureRetour));
                    finalGroupes.add(windowGroup);
                    groupeIdCounter++;
                } else {
                    // Aucun véhicule disponible avec assez de places
                    // Essayer de splitter le groupe en sous-groupes
                    // Pour l'instant, marquer comme sans véhicule
                    int ordre = 1;
                    for (Reservation r : windowGroup) {
                        r.setGroupeId(groupeIdCounter);
                        r.setOrdreLivraison(ordre++);
                        r.setHeureDepartAeroport(heureDepart);
                        r.setHeureRetourAeroport(null);
                    }
                    finalGroupes.add(windowGroup);
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
