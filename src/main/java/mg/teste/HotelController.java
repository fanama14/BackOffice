package mg.teste;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.annotations.POST;
import mg.framework.annotations.RequestParam;
import mg.framework.annotations.RestAPI;
import mg.framework.ModelView;

import com.backoffice.dao.HotelDAO;
import com.backoffice.dao.LieuxDAO;
import com.backoffice.model.Hotel;
import com.backoffice.model.Lieux;

import java.util.List;

@Controller
public class HotelController {

    private final HotelDAO hotelDAO = new HotelDAO();
    private final LieuxDAO lieuxDAO = new LieuxDAO();

    @GET("hotel/list")
    public ModelView list() {
        ModelView mv = new ModelView("hotel-list");
        try {
            List<Hotel> hotels = hotelDAO.findAll();
            mv.addData("hotels", hotels);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement des hôtels : " + e.getMessage());
        }
        return mv;
    }

    @GET("hotel/form")
    public ModelView showForm() {
        ModelView mv = new ModelView("hotel-form");
        mv.addData("hotel", null);
        mv.addData("isEdit", false);
        try {
            List<Lieux> lieuxList = lieuxDAO.findAll();
            mv.addData("lieuxList", lieuxList);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement des lieux : " + e.getMessage());
        }
        return mv;
    }

    @GET("hotel/edit")
    public ModelView showEditForm(@RequestParam("id") int id) {
        ModelView mv = new ModelView("hotel-form");
        try {
            Hotel hotel = hotelDAO.findById(id);
            if (hotel != null) {
                mv.addData("hotel", hotel);
                mv.addData("isEdit", true);
            } else {
                mv.addData("error", "Hôtel non trouvé");
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

    @POST("hotel/save")
    public ModelView save(
            @RequestParam("id") String idStr,
            @RequestParam("nom") String nom,
            @RequestParam("adresse") String adresse,
            @RequestParam("ville") String ville,
            @RequestParam("lieuxId") int lieuxId) {

        ModelView mv = new ModelView("hotel-form");
        try {
            Hotel hotel = new Hotel();
            hotel.setNom(nom);
            hotel.setAdresse(adresse);
            hotel.setVille(ville);
            hotel.setLieuxId(lieuxId);

            Integer id = parseIntOrNull(idStr);

            if (id != null && id > 0) {
                hotel.setId(id);
                hotelDAO.update(hotel);
                mv.addData("success", "Hôtel modifié avec succès !");
                mv.addData("isEdit", true);
            } else {
                hotelDAO.insert(hotel);
                mv.addData("success", "Hôtel ajouté avec succès !");
                mv.addData("isEdit", false);
            }
            mv.addData("hotel", hotel);

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

    @GET("hotel/delete")
    public ModelView delete(@RequestParam("id") int id) {
        ModelView mv = new ModelView("hotel-list");
        try {
            hotelDAO.delete(id);
            mv.addData("success", "Hôtel supprimé avec succès !");
            List<Hotel> hotels = hotelDAO.findAll();
            mv.addData("hotels", hotels);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors de la suppression : " + e.getMessage());
            try {
                mv.addData("hotels", hotelDAO.findAll());
            } catch (Exception ex) {
                // ignore
            }
        }
        return mv;
    }

    @GET("api/hotel/list")
    @RestAPI
    public List<Hotel> listJSON() {
        try {
            return hotelDAO.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des hôtels", e);
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
