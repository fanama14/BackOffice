package com.backoffice.controller;

import mg.framework.annotations.Controller;
import mg.framework.annotations.GET;
import mg.framework.annotations.POST;
import mg.framework.annotations.RequestParam;
import mg.framework.ModelView;

@Controller
public class ExampleController {

    @GET("/home")
    public ModelView home() {
        ModelView mv = new ModelView("home.jsp");
        mv.addData("message", "Bienvenue sur BackOffice");
        return mv;
    }

    @POST("/login")
    public ModelView login(@RequestParam("username") String username,
            @RequestParam("password") String password) {
        ModelView mv = new ModelView("dashboard.jsp");
        // Logique d'authentification
        mv.addData("user", username);
        return mv;
    }
}