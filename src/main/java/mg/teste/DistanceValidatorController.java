package mg.teste;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.ModelView;

import com.backoffice.dao.DistanceDAO;
import com.backoffice.dao.LieuxDAO;
import com.backoffice.util.DistanceValidator;

@Controller
public class DistanceValidatorController {

    private final DistanceDAO distanceDAO = new DistanceDAO();
    private final LieuxDAO lieuxDAO = new LieuxDAO();

    /**
     * Affiche un rapport sur les distances manquantes
     */
    @GET("distance-validator")
    public ModelView validateDistances() {
        ModelView mv = new ModelView("index");

        try {
            DistanceValidator validator = new DistanceValidator(distanceDAO, lieuxDAO);
            String report = validator.generateMissingDistancesReport();

            mv.addData("distanceReport", report);
            mv.addData("message", report);

        } catch (Exception e) {
            mv.addData("error", "Erreur lors de la validation : " + e.getMessage());
            e.printStackTrace();
        }

        return mv;
    }
}
