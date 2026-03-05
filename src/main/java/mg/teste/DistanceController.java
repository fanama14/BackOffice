package mg.teste;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.annotations.POST;
import mg.framework.annotations.RequestParam;
import mg.framework.annotations.RestAPI;
import mg.framework.ModelView;

import com.backoffice.dao.DistanceDAO;
import com.backoffice.dao.LieuxDAO;
import com.backoffice.model.Distance;
import com.backoffice.model.Lieux;

import java.util.List;

@Controller
public class DistanceController {

    private final DistanceDAO distanceDAO = new DistanceDAO();
    private final LieuxDAO lieuxDAO = new LieuxDAO();

    @GET("distance/list")
    public ModelView list() {
        ModelView mv = new ModelView("distance-list");
        try {
            List<Distance> distances = distanceDAO.findAll();
            mv.addData("distances", distances);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement des distances : " + e.getMessage());
        }
        return mv;
    }

    @GET("distance/form")
    public ModelView showForm() {
        ModelView mv = new ModelView("distance-form");
        mv.addData("distance", null);
        mv.addData("isEdit", false);
        try {
            List<Lieux> lieuxList = lieuxDAO.findAll();
            mv.addData("lieuxList", lieuxList);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement des lieux : " + e.getMessage());
        }
        return mv;
    }

    @GET("distance/edit")
    public ModelView showEditForm(@RequestParam("id") int id) {
        ModelView mv = new ModelView("distance-form");
        try {
            Distance distance = distanceDAO.findById(id);
            if (distance != null) {
                mv.addData("distance", distance);
                mv.addData("isEdit", true);
            } else {
                mv.addData("error", "Distance non trouvée");
                mv.addData("isEdit", false);
            }
            List<Lieux> lieuxList = lieuxDAO.findAll();
            mv.addData("lieuxList", lieuxList);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement : " + e.getMessage());
            mv.addData("isEdit", false);
        }
        return mv;
    }

    @POST("distance/save")
    public ModelView save(
            @RequestParam("id") String idStr,
            @RequestParam("lieuxFrom") int lieuxFrom,
            @RequestParam("lieuxTo") int lieuxTo,
            @RequestParam("valeur") double valeur) {

        ModelView mv = new ModelView("distance-form");
        try {
            Distance distance = new Distance();
            distance.setLieuxFrom(lieuxFrom);
            distance.setLieuxTo(lieuxTo);
            distance.setValeur(valeur);

            Integer id = parseIntOrNull(idStr);

            if (id != null && id > 0) {
                distance.setId(id);
                distanceDAO.update(distance);
                mv.addData("success", "Distance modifiée avec succès !");
                mv.addData("isEdit", true);
            } else {
                distanceDAO.insert(distance);
                mv.addData("success", "Distance ajoutée avec succès !");
                mv.addData("isEdit", false);
            }
            mv.addData("distance", distance);

        } catch (Exception e) {
            mv.addData("error", "Erreur lors de l'enregistrement : " + e.getMessage());
            mv.addData("isEdit", idStr != null && !idStr.isEmpty());
        }

        try {
            List<Lieux> lieuxList = lieuxDAO.findAll();
            mv.addData("lieuxList", lieuxList);
        } catch (Exception e) {
            // ignore
        }
        return mv;
    }

    @GET("distance/delete")
    public ModelView delete(@RequestParam("id") int id) {
        ModelView mv = new ModelView("distance-list");
        try {
            distanceDAO.delete(id);
            mv.addData("success", "Distance supprimée avec succès !");
            List<Distance> distances = distanceDAO.findAll();
            mv.addData("distances", distances);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors de la suppression : " + e.getMessage());
            try {
                mv.addData("distances", distanceDAO.findAll());
            } catch (Exception ex) {
                // ignore
            }
        }
        return mv;
    }

    @GET("api/distance/list")
    @RestAPI
    public List<Distance> listJSON() {
        try {
            return distanceDAO.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des distances", e);
        }
    }

    private Integer parseIntOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
