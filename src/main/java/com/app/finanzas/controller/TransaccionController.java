package com.app.finanzas.controller;

import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.TipoTransaccion;
import com.app.finanzas.entity.Transaccion;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.CuentaService;
import com.app.finanzas.service.FondoService;
import com.app.finanzas.service.MonedaService;
import com.app.finanzas.service.PresupuestoService;
import com.app.finanzas.service.TransaccionService;
import com.app.finanzas.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/transacciones")
public class TransaccionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransaccionController.class);

    private static final List<String> CATEGORIAS_INGRESO = List.of(
            "Salario", "Dividendos", "Freelance", "Alquiler", "Ventas", "Bono", "Reembolso", "Otro"
    );

    private final TransaccionService transaccionService;
    private final CuentaService cuentaService;
    private final UsuarioService usuarioService;
    private final PresupuestoService presupuestoService;
    private final FondoService fondoService;
    private final MonedaService monedaService;

    public TransaccionController(TransaccionService transaccionService,
                                 CuentaService cuentaService,
                                 UsuarioService usuarioService,
                                 PresupuestoService presupuestoService,
                                 FondoService fondoService,
                                 MonedaService monedaService) {
        this.transaccionService = transaccionService;
        this.cuentaService = cuentaService;
        this.usuarioService = usuarioService;
        this.presupuestoService = presupuestoService;
        this.fondoService = fondoService;
        this.monedaService = monedaService;
    }

    @GetMapping
    public String listar(Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Transaccion transaccion = new Transaccion();
        transaccion.setFecha(LocalDate.now());
        Integer activaId = (Integer) session.getAttribute("cuentaActivaId");
        if (activaId != null) {
            Cuenta cuentaActiva = new Cuenta();
            cuentaActiva.setId(activaId);
            transaccion.setCuenta(cuentaActiva);
        }
        prepararModelo(model, usuario, transaccion, session);
        return "transacciones/lista";
    }

    @PostMapping
    public String crear(@Valid Transaccion transaccion,
                        BindingResult bindingResult,
                        @RequestParam(value = "presupuestoId", required = false) String presupuestoIdStr,
                        @RequestParam(value = "fondoId", required = false) String fondoIdStr,
                        @RequestParam(value = "redirect", required = false) String redirect,
                        RedirectAttributes redirectAttributes,
                        Model model,
                        HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado();

        // Auto-asignar fecha si no viene del formulario (modo simplificado)
        if (transaccion.getFecha() == null) {
            transaccion.setFecha(LocalDate.now());
        }

        // Si el form no manda cuenta, usar la cuenta activa de sesion
        if (transaccion.getCuenta() == null || transaccion.getCuenta().getId() == null) {
            Integer activaId = (Integer) session.getAttribute("cuentaActivaId");
            if (activaId != null) {
                Cuenta cuentaActiva = new Cuenta();
                cuentaActiva.setId(activaId);
                transaccion.setCuenta(cuentaActiva);
            }
        }

        if (bindingResult.hasErrors()) {
            LOGGER.warn("Errores de validacion al crear transaccion: {}", bindingResult.getAllErrors());
            prepararModelo(model, usuario, transaccion, session);
            model.addAttribute("formConErrores", true);
            model.addAttribute("mensajeError",
                    "Revisa los campos: " + bindingResult.getFieldErrors().stream()
                            .map(e -> e.getField() + " " + e.getDefaultMessage())
                            .collect(Collectors.joining("; ")));
            return "transacciones/lista";
        }

        if (transaccion.getCuenta() == null || transaccion.getCuenta().getId() == null) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "No hay una cuenta activa. Crea una cuenta antes de registrar movimientos.");
            return "redirect:/transacciones";
        }

        Cuenta cuenta = cuentaService.buscarPorIdYUsuario(transaccion.getCuenta().getId(), usuario)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
        transaccion.setCuenta(cuenta);

        // Vincular presupuesto si fue seleccionado y pertenece al usuario
        if (presupuestoIdStr != null && !presupuestoIdStr.isBlank()) {
            try {
                Integer presupuestoId = Integer.parseInt(presupuestoIdStr);
                presupuestoService.listarActivosPorUsuario(usuario).stream()
                        .filter(p -> p.getId().equals(presupuestoId))
                        .findFirst()
                        .ifPresent(p -> {
                            transaccion.setPresupuesto(p);
                            transaccion.setFijo(p.getTipo() == com.app.finanzas.entity.Presupuesto.Tipo.FIJO);
                            if (transaccion.getCategoria() == null || transaccion.getCategoria().isBlank()) {
                                transaccion.setCategoria(p.getNombre());
                            }
                        });
            } catch (NumberFormatException ignored) { }
        }

        // Vincular fondo si fue seleccionado y pertenece al usuario (aporte a bolsa de ahorro)
        if (fondoIdStr != null && !fondoIdStr.isBlank()) {
            try {
                Integer fondoId = Integer.parseInt(fondoIdStr);
                fondoService.listarActivosPorUsuario(usuario).stream()
                        .filter(f -> f.getId().equals(fondoId))
                        .findFirst()
                        .ifPresent(f -> {
                            transaccion.setFondo(f);
                            // Forzar tipo GASTO (un aporte es un gasto contra la cuenta activa)
                            transaccion.setTipo(TipoTransaccion.GASTO);
                            if (transaccion.getCategoria() == null || transaccion.getCategoria().isBlank()) {
                                transaccion.setCategoria("Ahorro: " + f.getNombre());
                            }
                        });
            } catch (NumberFormatException ignored) { }
        }

        // Fallback de categoria si quedo vacia
        if (transaccion.getCategoria() == null || transaccion.getCategoria().isBlank()) {
            transaccion.setCategoria(transaccion.getTipo() == TipoTransaccion.INGRESO ? "Ingreso" : "Gasto general");
        }

        try {
            transaccionService.registrar(transaccion);
            redirectAttributes.addFlashAttribute("mensajeExito", "Movimiento registrado");
        } catch (RuntimeException ex) {
            LOGGER.error("Fallo al registrar transaccion", ex);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "No se pudo guardar el movimiento: " + ex.getMessage());
        }
        // Whitelist de redirects validos (evita open-redirect)
        if (redirect != null && (redirect.equals("/presupuestos")
                || redirect.equals("/fondos")
                || redirect.equals("/dashboard")
                || redirect.equals("/transacciones"))) {
            return "redirect:" + redirect;
        }
        return "redirect:/transacciones";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuarioAutenticado();
        transaccionService.buscarPorIdYUsuario(id, usuario)
                .ifPresent(t -> transaccionService.eliminar(t.getId()));
        redirectAttributes.addFlashAttribute("mensajeExito", "Movimiento eliminado");
        return "redirect:/transacciones";
    }

    private void prepararModelo(Model model, Usuario usuario, Transaccion transaccion, HttpSession session) {
        Integer activaId = (Integer) session.getAttribute("cuentaActivaId");

        // Solo transacciones de la cuenta activa (vista estilo Nubank: una cuenta a la vez)
        List<Transaccion> todas = activaId != null
                ? transaccionService.listarPorCuenta(activaId)
                : Collections.emptyList();

        // Agrupar por fecha (LinkedHashMap mantiene orden DESC por fecha)
        Map<LocalDate, List<Transaccion>> agrupadas = new LinkedHashMap<>();
        for (Transaccion t : todas) {
            agrupadas.computeIfAbsent(t.getFecha(), k -> new ArrayList<>()).add(t);
        }

        // Resumen del mes actual
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        List<Transaccion> esteMes = todas.stream()
                .filter(t -> !t.getFecha().isBefore(inicioMes) && !t.getFecha().isAfter(finMes))
                .collect(Collectors.toList());

        BigDecimal totalIngresosEsteMes = esteMes.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.INGRESO)
                .map(Transaccion::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGastosEsteMes = esteMes.stream()
                .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
                .map(Transaccion::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Saldo de la cuenta activa
        List<Cuenta> cuentasUsuario = cuentaService.listarPorUsuario(usuario);
        BigDecimal saldoCuentaActiva = cuentasUsuario.stream()
                .filter(c -> c.getId().equals(activaId))
                .map(Cuenta::getSaldo)
                .findFirst()
                .orElseGet(() -> cuentasUsuario.isEmpty() ? BigDecimal.ZERO : cuentasUsuario.get(0).getSaldo());

        // Conversion de moneda (BD en COP, vista en moneda activa)
        String moneda = monedaService.normalizar((String) session.getAttribute("monedaPreferida"));
        BigDecimal saldoConvertido = monedaService.convertir(saldoCuentaActiva, moneda);
        BigDecimal totalIngresosConvertido = monedaService.convertir(totalIngresosEsteMes, moneda);
        BigDecimal totalGastosConvertido = monedaService.convertir(totalGastosEsteMes, moneda);

        model.addAttribute("transaccion", transaccion);
        model.addAttribute("agrupadas", agrupadas);
        model.addAttribute("totalIngresosEsteMes", totalIngresosConvertido);
        model.addAttribute("totalGastosEsteMes", totalGastosConvertido);
        model.addAttribute("saldoCuentaActiva", saldoConvertido);
        model.addAttribute("presupuestos", presupuestoService.listarActivosPorUsuario(usuario));
        model.addAttribute("categoriasIngreso", CATEGORIAS_INGRESO);
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
