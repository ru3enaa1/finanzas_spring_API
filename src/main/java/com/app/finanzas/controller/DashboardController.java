package com.app.finanzas.controller;

import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.GastoFijo;
import com.app.finanzas.entity.RegistroGastoFijo;
import com.app.finanzas.entity.Transaccion;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.CuentaService;
import com.app.finanzas.service.GastoFijoService;
import com.app.finanzas.service.RegistroGastoFijoService;
import com.app.finanzas.service.TransaccionService;
import com.app.finanzas.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final UsuarioService usuarioService;
    private final CuentaService cuentaService;
    private final TransaccionService transaccionService;
    private final GastoFijoService gastoFijoService;
    private final RegistroGastoFijoService registroGastoFijoService;

    public DashboardController(UsuarioService usuarioService,
                               CuentaService cuentaService,
                               TransaccionService transaccionService,
                               GastoFijoService gastoFijoService,
                               RegistroGastoFijoService registroGastoFijoService) {
        this.usuarioService = usuarioService;
        this.cuentaService = cuentaService;
        this.transaccionService = transaccionService;
        this.gastoFijoService = gastoFijoService;
        this.registroGastoFijoService = registroGastoFijoService;
    }

    @GetMapping({"/dashboard"})
    public String mostrarDashboard(Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();
        List<Cuenta> cuentas = cuentaService.listarPorUsuario(usuario);
        BigDecimal saldoTotal = cuentas.stream()
                .map(Cuenta::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        YearMonth periodoActual = YearMonth.now();
        List<GastoFijo> gastosPendientes = gastoFijoService.listarActivosPorUsuario(usuario).stream()
                .filter(gasto -> estaPendiente(gasto, periodoActual))
                .collect(Collectors.toList());

        List<Transaccion> transaccionesRecientes = transaccionService.listarRecientesPorUsuario(usuario, 5);

        model.addAttribute("usuario", usuario);
        model.addAttribute("cuentas", cuentas);
        model.addAttribute("saldoTotal", saldoTotal);
        model.addAttribute("periodoActual", periodoActual);
        model.addAttribute("anioActual", periodoActual.getYear());
        model.addAttribute("gastosPendientes", gastosPendientes);
        model.addAttribute("transaccionesRecientes", transaccionesRecientes);

        return "dashboard/index";
    }

    private boolean estaPendiente(GastoFijo gastoFijo, YearMonth periodo) {
        return registroGastoFijoService.buscarPorGastoYPeriodo(gastoFijo.getId(), periodo.getYear(), periodo.getMonthValue())
                .map(RegistroGastoFijo::getPagado)
                .map(pagado -> !pagado)
                .orElse(true);
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuario no autenticado");
        }
        return usuarioService.buscarPorCorreo(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
    }
}
