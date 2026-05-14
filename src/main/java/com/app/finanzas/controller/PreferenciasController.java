package com.app.finanzas.controller;

import com.app.finanzas.config.RutasModulos;
import com.app.finanzas.service.MonedaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/preferencias")
public class PreferenciasController {

    private final MonedaService monedaService;

    public PreferenciasController(MonedaService monedaService) {
        this.monedaService = monedaService;
    }

    @PostMapping("/moneda")
    public String cambiarMoneda(@RequestParam String moneda,
                                @RequestParam(required = false) String redirect,
                                HttpSession session) {
        if (monedaService.esValida(moneda)) {
            session.setAttribute("monedaPreferida", moneda);
        }
        return "redirect:" + RutasModulos.resolverODefault(redirect, "/dashboard");
    }
}
