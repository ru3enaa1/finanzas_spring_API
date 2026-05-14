package com.app.finanzas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AlertaController {

    @GetMapping("/alertas")
    public String mostrarAlertas() {
        return "alertas/lista";
    }
}
