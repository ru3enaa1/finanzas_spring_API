package com.app.finanzas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InformeController {

    @GetMapping("/informes")
    public String mostrarInformes() {
        return "informes/lista";
    }
}
