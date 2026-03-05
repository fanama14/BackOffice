package mg.teste;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.ModelView;

@Controller
public class IndexController {

    @GET("")
    public ModelView index() {
        return new ModelView("index");
    }
}
