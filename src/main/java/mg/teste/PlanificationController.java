package mg.teste;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.annotations.RequestParam;
import mg.framework.ModelView;

import com.backoffice.dao.DistanceDAO;
import com.backoffice.dao.HotelDAO;
import com.backoffice.dao.ParametreDAO;
import com.backoffice.dao.PlanificationDAO;
import com.backoffice.dao.ReservationDAO;
import com.backoffice.dao.VehiculeDAO;
import com.backoffice.dao.AeroportDAO;
import com.backoffice.model.Parametre;
import com.backoffice.model.Planification;
import com.backoffice.model.Reservation;
import com.backoffice.model.ReservationGroup;
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
    private final PlanificationDAO planificationDAO = new PlanificationDAO();

    @GET("planification")
    public ModelView showPlanification(
            @RequestParam(value = "dateDebut", required = false) String dateDebut,
            @RequestParam(value = "dateFin", required = false) String dateFin,
            @RequestParam(value = "action", required = false) String action) {

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

            String mode = (action == null || action.trim().isEmpty()) ? "filtrer" : action.trim().toLowerCase();

            List<Reservation> result;

            if ("planifier".equals(mode) || "filtrer".equals(mode)) {
                // Filtrer déclenche aussi le recalcul + sauvegarde pour garantir
                // que les planifications existent toujours sur la période demandée.
                List<Reservation> reservations = reservationDAO.findByPeriode(tsDebut, tsFin);

                GroupingService service = new GroupingService(vehiculeDAO, hotelDAO, aeroportDAO, distanceDAO,
                        planificationDAO);
                List<ReservationGroup> groups = service.planifier(reservations, parametre);

                result = new ArrayList<>();
                List<Planification> planifications = new ArrayList<>();

                for (ReservationGroup group : groups) {
                    for (Reservation r : group.getReservations()) {
                        result.add(r);

                        Planification p = new Planification();
                        p.setReservationId(r.getId());
                        p.setNbPassagersAffectes(r.getNombrePassager());
                        p.setGroupeId(r.getGroupeId());
                        p.setOrdreLivraison(r.getOrdreLivraison());
                        p.setHeureDepart(r.getHeureDepartAeroport());
                        p.setHeureRetour(r.getHeureRetourAeroport());
                        if (group.getVehicule() != null) {
                            p.setVehiculeId(group.getVehicule().getId());
                        }
                        planifications.add(p);
                    }
                }

                planificationDAO.deleteByPeriode(tsDebut, tsFin);
                if (!planifications.isEmpty()) {
                    planificationDAO.insertBatch(planifications);
                }

                if ("planifier".equals(mode)) {
                    mv.addData("success", "Planification recalculée et enregistrée.");
                }
            } else {
                // Mode inconnu: fallback sur les planifications déjà enregistrées.
                result = planificationDAO.findReservationsByPeriode(tsDebut, tsFin);
            }

            mv.addData("reservations", result);
            mv.addData("dateDebut", tsDebut.toString().substring(0, 10));
            mv.addData("dateFin", tsFin.toString().substring(0, 10));
            mv.addData("action", mode);

        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement : " + e.getMessage());
            e.printStackTrace();
        }

        return mv;
    }
}
