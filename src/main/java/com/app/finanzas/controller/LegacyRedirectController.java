package com.app.finanzas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Redirige rutas legacy a sus nuevas ubicaciones.
 */
@Controller
public class LegacyRedirectController {

    @GetMapping({"/gastos", "/gastos/**", "/gastos-fijos", "/gastos-fijos/**"})
    public String redirectGastos() {
        return "redirect:/presupuestos";
    }
}
