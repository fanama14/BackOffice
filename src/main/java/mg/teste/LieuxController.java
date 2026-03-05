package mg.teste;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.annotations.POST;
import mg.framework.annotations.RequestParam;
import mg.framework.annotations.RestAPI;
import mg.framework.ModelView;

import com.backoffice.dao.LieuxDAO;
import com.backoffice.model.Lieux;

import java.util.List;

@Controller
public class LieuxController {

    private final LieuxDAO lieuxDAO = new LieuxDAO();

    @GET("lieux/list")
    public ModelView list() {
        ModelView mv = new ModelView("lieux-list");
        try {
            List<Lieux> lieuxList = lieuxDAO.findAll();
            mv.addData("lieuxList", lieuxList);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement des lieux : " + e.getMessage());
        }
        return mv;
    }

    @GET("lieux/form")
    public ModelView showForm() {
        ModelView mv = new ModelView("lieux-form");
        mv.addData("lieux", null);
        mv.addData("isEdit", false);
        return mv;
    }

    @GET("lieux/edit")
    public ModelView showEditForm(@RequestParam("id") int id) {
        ModelView mv = new ModelView("lieux-form");
        try {
            Lieux lieux = lieuxDAO.findById(id);
            if (lieux != null) {
                mv.addData("lieux", lieux);
                mv.addData("isEdit", true);
            } else {
                mv.addData("error", "Lieu non trouvé");
                mv.addData("isEdit", false);
            }
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement du lieu : " + e.getMessage());
            mv.addData("isEdit", false);
        }
        return mv;
    }

    @POST("lieux/save")
    public ModelView save(
            @RequestParam("id") String idStr,
            @RequestParam("lieu") String lieu) {

        ModelView mv = new ModelView("lieux-form");
        try {
            Lieux lieux = new Lieux();
            lieux.setLieu(lieu);

            Integer id = parseIntOrNull(idStr);

            if (id != null && id > 0) {
                lieux.setId(id);
                lieuxDAO.update(lieux);
                mv.addData("success", "Lieu modifié avec succès !");
                mv.addData("isEdit", true);
            } else {
                lieuxDAO.insert(lieux);
                mv.addData("success", "Lieu ajouté avec succès !");
                mv.addData("isEdit", false);
            }
            mv.addData("lieux", lieux);

        } catch (Exception e) {
            mv.addData("error", "Erreur lors de l'enregistrement : " + e.getMessage());
            mv.addData("isEdit", idStr != null && !idStr.isEmpty());
        }
        return mv;
    }

    @GET("lieux/delete")
    public ModelView delete(@RequestParam("id") int id) {
        ModelView mv = new ModelView("lieux-list");
        try {
            lieuxDAO.delete(id);
            mv.addData("success", "Lieu supprimé avec succès !");
            List<Lieux> lieuxList = lieuxDAO.findAll();
            mv.addData("lieuxList", lieuxList);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors de la suppression : " + e.getMessage());
            try {
                mv.addData("lieuxList", lieuxDAO.findAll());
            } catch (Exception ex) {
                // ignore
            }
        }
        return mv;
    }

    @GET("api/lieux/list")
    @RestAPI
    public List<Lieux> listJSON() {
        try {
            return lieuxDAO.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des lieux", e);
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
