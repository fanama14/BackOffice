package mg.teste;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.annotations.POST;
import mg.framework.annotations.RequestParam;
import mg.framework.annotations.RestAPI;
import mg.framework.ModelView;

import com.backoffice.dao.HotelDAO;
import com.backoffice.dao.AeroportDAO;
import com.backoffice.dao.ReservationDAO;
import com.backoffice.model.Reservation;
import com.backoffice.model.Hotel;
import com.backoffice.model.Aeroport;

import java.sql.Timestamp;
import java.util.List;

@Controller
public class ReservationController {

    private final HotelDAO hotelDAO = new HotelDAO();
    private final AeroportDAO aeroportDAO = new AeroportDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();

    @GET("reservation/form")
    public ModelView showForm() {
        ModelView mv = new ModelView("reservation-form");
        try {
            List<Hotel> hotels = hotelDAO.findAll();
            mv.addData("hotels", hotels);
            List<Aeroport> aeroports = aeroportDAO.findAll();
            mv.addData("aeroports", aeroports);
        } catch (Exception e) {
            mv.addData("error", "Erreur lors du chargement : " + e.getMessage());
        }
        return mv;
    }

    @POST("reservation/save")
    public ModelView save(@RequestParam("clientId") String clientId,
            @RequestParam("nombrePassager") int nombrePassager,
            @RequestParam("dateArrivee") String dateArrivee,
            @RequestParam("hotelId") int hotelId,
            @RequestParam("aeroportId") int aeroportId) {
        ModelView mv = new ModelView("reservation-form");
        try {
            Reservation reservation = new Reservation();
            reservation.setClientId(clientId);
            reservation.setNombrePassager(nombrePassager);
            reservation.setDateArrivee(Timestamp.valueOf(dateArrivee.replace("T", " ") + ":00"));
            reservation.setHotelId(hotelId);
            reservation.setAeroportId(aeroportId);

            reservationDAO.insert(reservation);
            mv.addData("success", "Réservation enregistrée avec succès !");

        } catch (Exception e) {
            mv.addData("error", "Erreur lors de l'enregistrement : " + e.getMessage());
        }

        try {
            List<Hotel> hotels = hotelDAO.findAll();
            mv.addData("hotels", hotels);
            List<Aeroport> aeroports = aeroportDAO.findAll();
            mv.addData("aeroports", aeroports);
        } catch (Exception e) {
            // ignore
        }

        return mv;
    }

    @GET("reservation/delete")
    public ModelView delete(@RequestParam("id") int id) {
        ModelView mv = new ModelView("reservation-form");
        try {
            reservationDAO.delete(id);
            mv.addData("success", "Réservation supprimée avec succès !");
        } catch (Exception e) {
            mv.addData("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        try {
            List<Hotel> hotels = hotelDAO.findAll();
            mv.addData("hotels", hotels);
            List<Aeroport> aeroports = aeroportDAO.findAll();
            mv.addData("aeroports", aeroports);
        } catch (Exception e) {
            // ignore
        }
        return mv;
    }

    @GET("api/reservation/list")
    @RestAPI
    public List<Reservation> listJSON() {
        try {
            return reservationDAO.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des réservations", e);
        }
    }
}
