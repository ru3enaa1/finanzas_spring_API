package com.app.finanzas.controller;

import com.app.finanzas.config.RutasModulos;
import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.CuentaService;
import com.app.finanzas.service.MonedaService;
import com.app.finanzas.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cuentas")
public class CuentaController {

    private static final List<String> TIPOS_CUENTA = List.of(
            "Corriente", "Ahorro", "Tarjeta de credito", "Efectivo", "Inversion"
    );

    private static final int LIMITE_CUENTAS = 5;

    private final CuentaService cuentaService;
    private final UsuarioService usuarioService;
    private final MonedaService monedaService;

    public CuentaController(CuentaService cuentaService,
                            UsuarioService usuarioService,
                            MonedaService monedaService) {
        this.cuentaService = cuentaService;
        this.usuarioService = usuarioService;
        this.monedaService = monedaService;
    }

    @GetMapping
    public String listar(Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado();
        prepararModelo(model, session, usuario, new Cuenta());
        return "cuentas/lista";
    }

    @PostMapping
    public String crear(@Valid Cuenta cuenta,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model,
                        HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (bindingResult.hasErrors()) {
            prepararModelo(model, session, usuario, cuenta);
            model.addAttribute("formConErrores", true);
            return "cuentas/lista";
        }
        List<Cuenta> existentes = cuentaService.listarPorUsuario(usuario);
        if (existentes.size() >= LIMITE_CUENTAS) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "Maximo " + LIMITE_CUENTAS + " cuentas permitidas por usuario");
            return "redirect:/cuentas";
        }
        cuenta.setUsuario(usuario);
        // saldo_inicial = saldo en la creacion: lo que el usuario teclea es ambos
        cuenta.setSaldoInicial(cuenta.getSaldo());
        cuentaService.crear(cuenta);
        redirectAttributes.addFlashAttribute("mensajeExito", "Cuenta creada correctamente");
        return "redirect:/cuentas";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizar(@PathVariable Integer id,
                             @Valid Cuenta cuenta,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (bindingResult.hasErrors()) {
            cuenta.setId(id);
            prepararModelo(model, session, usuario, cuenta);
            model.addAttribute("formConErrores", true);
            return "cuentas/lista";
        }
        cuentaService.buscarPorIdYUsuario(id, usuario).ifPresent(existente -> {
            existente.setNombre(cuenta.getNombre());
            existente.setTipo(cuenta.getTipo());
            // Si el saldo cambio, ajustar saldoInicial para que saldo = saldoInicial + transacciones
            if (existente.getSaldo().compareTo(cuenta.getSaldo()) != 0) {
                BigDecimal diff = cuenta.getSaldo().subtract(existente.getSaldo());
                existente.setSaldoInicial(existente.getSaldoInicial().add(diff));
                existente.setSaldo(cuenta.getSaldo());
            }
            cuentaService.actualizar(existente);
        });
        redirectAttributes.addFlashAttribute("mensajeExito", "Cuenta actualizada");
        return "redirect:/cuentas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuarioAutenticado();
        cuentaService.buscarPorIdYUsuario(id, usuario).ifPresent(c -> {
            cuentaService.eliminar(c.getId());
            Integer activaId = (Integer) session.getAttribute("cuentaActivaId");
            if (c.getId().equals(activaId)) {
                session.removeAttribute("cuentaActivaId");
            }
        });
        redirectAttributes.addFlashAttribute("mensajeExito", "Cuenta eliminada");
        return "redirect:/cuentas";
    }

    @PostMapping("/{id}/saldo")
    public String ajustarSaldo(@PathVariable Integer id,
                               @RequestParam BigDecimal saldo,
                               RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuarioAutenticado();
        cuentaService.buscarPorIdYUsuario(id, usuario)
                .ifPresent(c -> cuentaService.ajustarSaldo(c, saldo));
        redirectAttributes.addFlashAttribute("mensajeExito", "Saldo ajustado");
        return "redirect:/cuentas";
    }

    @PostMapping("/seleccionar")
    public String seleccionar(@RequestParam Integer cuentaId,
                              @RequestParam(required = false) String redirect,
                              HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado();
        cuentaService.buscarPorIdYUsuario(cuentaId, usuario)
                .ifPresent(c -> session.setAttribute("cuentaActivaId", c.getId()));
        return "redirect:" + RutasModulos.resolverODefault(redirect, "/cuentas");
    }

    private void prepararModelo(Model model, HttpSession session, Usuario usuario, Cuenta cuenta) {
        List<Cuenta> cuentas = cuentaService.listarPorUsuario(usuario);

        BigDecimal saldoTotal = cuentas.stream()
                .map(Cuenta::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String moneda = monedaService.normalizar((String) session.getAttribute("monedaPreferida"));

        // Cuenta activa: la guardada en sesión; si no existe o fue eliminada, usa la primera
        Integer activaId = (Integer) session.getAttribute("cuentaActivaId");
        Optional<Cuenta> cuentaActiva = cuentas.stream()
                .filter(c -> c.getId().equals(activaId))
                .findFirst();
        if (cuentaActiva.isEmpty() && !cuentas.isEmpty()) {
            cuentaActiva = Optional.of(cuentas.get(0));
            session.setAttribute("cuentaActivaId", cuentas.get(0).getId());
        }

        BigDecimal saldoActivo = cuentaActiva.map(Cuenta::getSaldo).orElse(BigDecimal.ZERO);
        BigDecimal saldoConvertido = monedaService.convertir(saldoActivo, moneda);
        BigDecimal saldoTotalConvertido = monedaService.convertir(saldoTotal, moneda);

        Map<Integer, Double> porcentajes = new LinkedHashMap<>();
        boolean hayBalance = saldoTotal.compareTo(BigDecimal.ZERO) > 0;
        for (Cuenta c : cuentas) {
            double pct = hayBalance
                    ? c.getSaldo()
                            .divide(saldoTotal, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue()
                    : 50.0;
            porcentajes.put(c.getId(), Math.max(10.0, Math.min(pct, 88.0)));
        }

        model.addAttribute("cuentas", cuentas);
        model.addAttribute("cuenta", cuenta);
        model.addAttribute("saldoTotal", saldoTotal);
        model.addAttribute("saldoConvertido", saldoConvertido);
        model.addAttribute("saldoTotalConvertido", saldoTotalConvertido);
        model.addAttribute("monedaActual", moneda);
        model.addAttribute("tiposCuenta", TIPOS_CUENTA);
        model.addAttribute("porcentajes", porcentajes);
        model.addAttribute("cuentaActivaId", cuentaActiva.map(Cuenta::getId).orElse(null));
        model.addAttribute("cuentaActivaNombre", cuentaActiva.map(Cuenta::getNombre).orElse(""));
        model.addAttribute("cuentasLlenas", cuentas.size() >= LIMITE_CUENTAS);
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
