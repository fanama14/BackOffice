package mg.teste;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.annotations.POST;
import mg.framework.annotations.RequestParam;
import mg.framework.annotations.RestAPI;
import mg.framework.ModelView;

import com.backoffice.dao.AeroportDAO;
import com.backoffice.dao.LieuxDAO;
import com.backoffice.model.Aeroport;
import com.backoffice.model.Lieux;

import java.util.List;

@Controller
public class AeroportController {

    private final AeroportDAO aeroportDAO = new AeroportDAO();
    private final LieuxDAO lieuxDAO = new LieuxDAO();

    @GET("aeroport/list")
    public ModelView list() {
        ModelView mv = new ModelView("aeroport-list");
        try {
            List<Aeroport> aeroports = aeroportDAO.findAll();
            mv.addData("aeroports", aeroports);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement des aéroports : " + e.getMessage());
        }
        return mv;
    }

    @GET("aeroport/form")
    public ModelView showForm() {
        ModelView mv = new ModelView("aeroport-form");
        mv.addData("aeroport", null);
        mv.addData("isEdit", false);
        try {
            List<Lieux> lieuxList = lieuxDAO.findAll();
            mv.addData("lieuxList", lieuxList);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement des lieux : " + e.getMessage());
        }
        return mv;
    }

    @GET("aeroport/edit")
    public ModelView showEditForm(@RequestParam("id") int id) {
        ModelView mv = new ModelView("aeroport-form");
        try {
            Aeroport aeroport = aeroportDAO.findById(id);
            if (aeroport != null) {
                mv.addData("aeroport", aeroport);
                mv.addData("isEdit", true);
            } else {
                mv.addData("error", "Aéroport non trouvé");
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

    @POST("aeroport/save")
    public ModelView save(
            @RequestParam("id") String idStr,
            @RequestParam("code") String code,
            @RequestParam("libelle") String libelle,
            @RequestParam("lieuxId") int lieuxId) {

        ModelView mv = new ModelView("aeroport-form");
        try {
            Aeroport aeroport = new Aeroport();
            aeroport.setCode(code);
            aeroport.setLibelle(libelle);
            aeroport.setLieuxId(lieuxId);

            Integer id = parseIntOrNull(idStr);

            if (id != null && id > 0) {
                aeroport.setId(id);
                aeroportDAO.update(aeroport);
                mv.addData("success", "Aéroport modifié avec succès !");
                mv.addData("isEdit", true);
            } else {
                aeroportDAO.insert(aeroport);
                mv.addData("success", "Aéroport ajouté avec succès !");
                mv.addData("isEdit", false);
            }
            mv.addData("aeroport", aeroport);

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

    @GET("aeroport/delete")
    public ModelView delete(@RequestParam("id") int id) {
        ModelView mv = new ModelView("aeroport-list");
        try {
            aeroportDAO.delete(id);
            mv.addData("success", "Aéroport supprimé avec succès !");
            List<Aeroport> aeroports = aeroportDAO.findAll();
            mv.addData("aeroports", aeroports);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors de la suppression : " + e.getMessage());
            try {
                mv.addData("aeroports", aeroportDAO.findAll());
            } catch (Exception ex) {
                // ignore
            }
        }
        return mv;
    }

    @GET("api/aeroport/list")
    @RestAPI
    public List<Aeroport> listJSON() {
        try {
            return aeroportDAO.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des aéroports", e);
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
