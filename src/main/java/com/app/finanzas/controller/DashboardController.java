package com.app.finanzas.controller;

import com.app.finanzas.entity.Alerta;
import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.Presupuesto;
import com.app.finanzas.entity.Transaccion;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.AlertaService;
import com.app.finanzas.service.CuentaService;
import com.app.finanzas.service.MonedaService;
import com.app.finanzas.service.PresupuestoService;
import com.app.finanzas.service.TransaccionService;
import com.app.finanzas.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final UsuarioService usuarioService;
    private final CuentaService cuentaService;
    private final TransaccionService transaccionService;
    private final PresupuestoService presupuestoService;
    private final AlertaService alertaService;
    private final MonedaService monedaService;

    public DashboardController(UsuarioService usuarioService,
                               CuentaService cuentaService,
                               TransaccionService transaccionService,
                               PresupuestoService presupuestoService,
                               AlertaService alertaService,
                               MonedaService monedaService) {
        this.usuarioService = usuarioService;
        this.cuentaService = cuentaService;
        this.transaccionService = transaccionService;
        this.presupuestoService = presupuestoService;
        this.alertaService = alertaService;
        this.monedaService = monedaService;
    }

    @GetMapping({"/dashboard"})
    public String mostrarDashboard(Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado();
        List<Cuenta> cuentas = cuentaService.listarPorUsuario(usuario);
        BigDecimal saldoTotal = cuentas.stream()
                .map(Cuenta::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        YearMonth periodoActual = YearMonth.now();
        Locale localeEsCo = new Locale("es", "CO");
        DateTimeFormatter fechaCortaFormatter = DateTimeFormatter.ofPattern("dd/MM").withLocale(localeEsCo);
        String mesActual = periodoActual.getMonth().getDisplayName(TextStyle.FULL, localeEsCo);

        // Presupuestos tipo FIJO con pago pendiente este mes (Arriendo, Netflix, etc.)
        List<Presupuesto> gastosPendientes = presupuestoService.listarActivosPorUsuario(usuario).stream()
                .filter(p -> p.getTipo() == Presupuesto.Tipo.FIJO)
                .filter(p -> estaPendiente(p, periodoActual))
                .collect(Collectors.toList());

        // Solo transacciones recientes de la cuenta activa (coherencia con /transacciones)
        Integer activaId = (Integer) session.getAttribute("cuentaActivaId");
        List<Transaccion> transaccionesRecientes = activaId != null
                ? transaccionService.listarRecientesPorCuenta(activaId, 5)
                : java.util.Collections.emptyList();
        List<Alerta> alertasRecientes = alertaService.listarRecientes(usuario);

        // Conversion segun moneda activa de la sesion
        String moneda = monedaService.normalizar((String) session.getAttribute("monedaPreferida"));
        BigDecimal saldoTotalConvertido = monedaService.convertir(saldoTotal, moneda);

        model.addAttribute("usuario", usuario);
        model.addAttribute("cuentas", cuentas);
        model.addAttribute("saldoTotal", saldoTotalConvertido);
        model.addAttribute("periodoActual", periodoActual);
        model.addAttribute("anioActual", periodoActual.getYear());
        model.addAttribute("mesActual", mesActual);
        model.addAttribute("formateadorFechaTransaccion", fechaCortaFormatter);
        model.addAttribute("gastosPendientes", gastosPendientes);
        model.addAttribute("transaccionesRecientes", transaccionesRecientes);
        model.addAttribute("alertasRecientes", alertasRecientes);

        return "dashboard/index";
    }

    private boolean estaPendiente(Presupuesto presupuesto, YearMonth periodo) {
        return presupuestoService.buscarPeriodo(presupuesto.getId(), periodo.getYear(), periodo.getMonthValue())
                .map(p -> !Boolean.TRUE.equals(p.getPagado()))
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
