package mg.teste;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.annotations.POST;
import mg.framework.annotations.RequestParam;
import mg.framework.annotations.RestAPI;
import mg.framework.ModelView;

import com.backoffice.dao.VehiculeDAO;
import com.backoffice.model.Vehicule;

import java.util.List;

@Controller
public class VehiculeController {

    private final VehiculeDAO vehiculeDAO = new VehiculeDAO();

    @GET("vehicule/list")
    public ModelView list(
            @RequestParam("search") String search,
            @RequestParam("typeCarburant") String typeCarburant,
            @RequestParam("nombrePlaceMin") String nombrePlaceMinStr,
            @RequestParam("nombrePlaceMax") String nombrePlaceMaxStr) {
        
        ModelView mv = new ModelView("vehicule-list");
        try {
            Integer nombrePlaceMin = parseIntOrNull(nombrePlaceMinStr);
            Integer nombrePlaceMax = parseIntOrNull(nombrePlaceMaxStr);

            List<Vehicule> vehicules = vehiculeDAO.findWithFilters(
                search, typeCarburant, nombrePlaceMin, nombrePlaceMax
            );
            mv.addData("vehicules", vehicules);
            mv.addData("search", search);
            mv.addData("typeCarburant", typeCarburant);
            mv.addData("nombrePlaceMin", nombrePlaceMinStr);
            mv.addData("nombrePlaceMax", nombrePlaceMaxStr);
            
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement des véhicules : " + e.getMessage());
        }
        return mv;
    }

    @GET("vehicule/form")
    public ModelView showForm() {
        ModelView mv = new ModelView("vehicule-form");
        mv.addData("vehicule", null);
        mv.addData("isEdit", false);
        return mv;
    }

    @GET("vehicule/edit")
    public ModelView showEditForm(@RequestParam("id") int id) {
        ModelView mv = new ModelView("vehicule-form");
        try {
            Vehicule vehicule = vehiculeDAO.findById(id);
            if (vehicule != null) {
                mv.addData("vehicule", vehicule);
                mv.addData("isEdit", true);
            } else {
                mv.addData("error", "Véhicule non trouvé");
                mv.addData("isEdit", false);
            }
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement du véhicule : " + e.getMessage());
            mv.addData("isEdit", false);
        }
        return mv;
    }

    @POST("vehicule/save")
    public ModelView save(
            @RequestParam("id") String idStr,
            @RequestParam("reference") String reference,
            @RequestParam("nombrePlace") int nombrePlace,
            @RequestParam("typeCarburant") String typeCarburant) {
        
        ModelView mv = new ModelView("vehicule-form");
        try {
            Vehicule vehicule = new Vehicule();
            vehicule.setReference(reference);
            vehicule.setNombrePlace(nombrePlace);
            vehicule.setTypeCarburant(typeCarburant);

            Integer id = parseIntOrNull(idStr);
            
            if (id != null && id > 0) {
                vehicule.setId(id);
                vehiculeDAO.update(vehicule);
                mv.addData("success", "Véhicule modifié avec succès !");
                mv.addData("isEdit", true);
            } else {
                vehiculeDAO.insert(vehicule);
                mv.addData("success", "Véhicule ajouté avec succès !");
                mv.addData("isEdit", false);
            }
            mv.addData("vehicule", vehicule);

        } catch (Exception e) {
            mv.addData("error", "Erreur lors de l'enregistrement : " + e.getMessage());
            mv.addData("isEdit", idStr != null && !idStr.isEmpty());
        }
        return mv;
    }

    @GET("vehicule/delete")
    public ModelView delete(@RequestParam("id") int id) {
        ModelView mv = new ModelView("vehicule-list");
        try {
            vehiculeDAO.delete(id);
            mv.addData("success", "Véhicule supprimé avec succès !");
            List<Vehicule> vehicules = vehiculeDAO.findAll();
            mv.addData("vehicules", vehicules);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors de la suppression : " + e.getMessage());
            try {
                mv.addData("vehicules", vehiculeDAO.findAll());
            } catch (Exception ex) {
                // ignore
            }
        }
        return mv;
    }

    @GET("api/vehicule/list")
    @RestAPI
    public List<Vehicule> listJSON() {
        try {
            return vehiculeDAO.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des véhicules", e);
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
