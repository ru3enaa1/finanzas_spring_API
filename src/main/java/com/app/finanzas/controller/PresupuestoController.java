package com.app.finanzas.controller;

import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.Presupuesto;
import com.app.finanzas.entity.PresupuestoPeriodo;
import com.app.finanzas.entity.TipoTransaccion;
import com.app.finanzas.entity.Transaccion;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.CuentaService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/presupuestos")
public class PresupuestoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PresupuestoController.class);

    private static final List<String> CATEGORIAS_SUGERIDAS = List.of(
            "Mercado", "Transporte", "Salidas", "Subscripciones digitales",
            "Gastos personales", "Arriendo", "Servicios", "Salud",
            "Educación", "Mascota", "Otros"
    );

    private static final Map<String, String> COLORES_POR_CATEGORIA = Map.ofEntries(
            Map.entry("Mercado",                  "#E67E22"),
            Map.entry("Transporte",               "#5B8DEF"),
            Map.entry("Salidas",                  "#E91E63"),
            Map.entry("Subscripciones digitales", "#9B59B6"),
            Map.entry("Gastos personales",        "#3498DB"),
            Map.entry("Arriendo",                 "#34495E"),
            Map.entry("Servicios",                "#16A085"),
            Map.entry("Salud",                    "#27AE60"),
            Map.entry("Educación",                "#F39C12"),
            Map.entry("Mascota",                  "#D35400")
    );

    private final PresupuestoService presupuestoService;
    private final TransaccionService transaccionService;
    private final UsuarioService usuarioService;
    private final CuentaService cuentaService;
    private final MonedaService monedaService;

    public PresupuestoController(PresupuestoService presupuestoService,
                                 TransaccionService transaccionService,
                                 UsuarioService usuarioService,
                                 CuentaService cuentaService,
                                 MonedaService monedaService) {
        this.presupuestoService = presupuestoService;
        this.transaccionService = transaccionService;
        this.usuarioService = usuarioService;
        this.cuentaService = cuentaService;
        this.monedaService = monedaService;
    }

    @GetMapping
    public String listar(Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Presupuesto nuevo = new Presupuesto();
        nuevo.setTipo(Presupuesto.Tipo.LIMITE);
        prepararModelo(model, usuario, nuevo, session);
        return "presupuestos/lista";
    }

    @PostMapping
    public String crear(@Valid Presupuesto presupuesto,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model,
                        HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado();

        // Auto-asignar color si no viene
        if (presupuesto.getColor() == null || presupuesto.getColor().isBlank()) {
            String color = COLORES_POR_CATEGORIA.getOrDefault(presupuesto.getNombre(), "#C4985A");
            presupuesto.setColor(color);
        }

        if (bindingResult.hasErrors()) {
            LOGGER.warn("Errores de validacion al crear presupuesto: {}", bindingResult.getAllErrors());
            prepararModelo(model, usuario, presupuesto, session);
            model.addAttribute("formConErrores", true);
            return "presupuestos/lista";
        }

        try {
            presupuesto.setUsuario(usuario);
            if (presupuesto.getCategoria() == null || presupuesto.getCategoria().isBlank()) {
                presupuesto.setCategoria(presupuesto.getNombre());
            }
            presupuestoService.registrar(presupuesto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Presupuesto creado");
        } catch (RuntimeException ex) {
            LOGGER.error("Fallo al crear presupuesto", ex);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "No se pudo crear el presupuesto: " + ex.getMessage());
        }
        return "redirect:/presupuestos";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizar(@PathVariable Integer id,
                             @Valid Presupuesto presupuesto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (bindingResult.hasErrors()) {
            presupuesto.setId(id);
            prepararModelo(model, usuario, presupuesto, session);
            model.addAttribute("formConErrores", true);
            return "presupuestos/lista";
        }
        presupuestoService.buscarPorIdYUsuario(id, usuario).ifPresent(existente -> {
            existente.setNombre(presupuesto.getNombre());
            existente.setMontoEstimado(presupuesto.getMontoEstimado());
            existente.setTipo(presupuesto.getTipo());
            if (presupuesto.getColor() != null && !presupuesto.getColor().isBlank()) {
                existente.setColor(presupuesto.getColor());
            }
            existente.setDiaVencimiento(presupuesto.getDiaVencimiento());
            existente.setDescripcion(presupuesto.getDescripcion());
            presupuestoService.actualizar(existente);
        });
        redirectAttributes.addFlashAttribute("mensajeExito", "Presupuesto actualizado");
        return "redirect:/presupuestos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuarioAutenticado();
        presupuestoService.buscarPorIdYUsuario(id, usuario)
                .ifPresent(p -> presupuestoService.eliminar(p.getId()));
        redirectAttributes.addFlashAttribute("mensajeExito", "Presupuesto eliminado");
        return "redirect:/presupuestos";
    }

    private void prepararModelo(Model model, Usuario usuario, Presupuesto formulario, HttpSession session) {
        List<Presupuesto> presupuestos = presupuestoService.listarActivosPorUsuario(usuario);

        YearMonth hoy = YearMonth.now();
        int anio = hoy.getYear();
        int mes = hoy.getMonthValue();

        String moneda = monedaService.normalizar((String) session.getAttribute("monedaPreferida"));
        BigDecimal tasa = monedaService.tasa(moneda);

        // Cuenta activa y saldo
        Integer activaId = (Integer) session.getAttribute("cuentaActivaId");
        BigDecimal saldoCuentaActiva = cuentaService.listarPorUsuario(usuario).stream()
                .filter(c -> c.getId().equals(activaId))
                .map(Cuenta::getSaldo)
                .findFirst()
                .orElse(BigDecimal.ZERO);
        BigDecimal saldoConvertido = monedaService.convertir(saldoCuentaActiva, moneda);

        // Transacciones del mes con presupuesto vinculado, SOLO de la cuenta activa
        List<Transaccion> todasUsuario = activaId != null
                ? transaccionService.listarPorCuenta(activaId)
                : new ArrayList<>();
        LocalDate inicioMes = LocalDate.of(anio, mes, 1);
        LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
        List<Transaccion> conPresupuesto = todasUsuario.stream()
                .filter(t -> t.getPresupuesto() != null)
                .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
                .filter(t -> !t.getFecha().isBefore(inicioMes) && !t.getFecha().isAfter(finMes))
                .toList();

        // Mapa: presupuestoId -> gastado en cuenta activa este mes (contexto dual)
        Map<Integer, BigDecimal> gastadoEnCuentaActiva = new java.util.HashMap<>();
        for (Transaccion t : conPresupuesto) {
            gastadoEnCuentaActiva.merge(t.getPresupuesto().getId(), t.getMonto(), BigDecimal::add);
        }

        // Pre-calcular vista de cada presupuesto (gastado global, contexto de cuenta, %, estado)
        List<PresupuestoVista> vistas = new ArrayList<>();
        BigDecimal totalGastadoPresupuestos = BigDecimal.ZERO;
        BigDecimal totalLimite = BigDecimal.ZERO;

        for (Presupuesto p : presupuestos) {
            BigDecimal gastado = presupuestoService.buscarPeriodo(p.getId(), anio, mes)
                    .map(PresupuestoPeriodo::getMontoGastado)
                    .orElse(BigDecimal.ZERO);
            BigDecimal limite = p.getMontoEstimado() != null ? p.getMontoEstimado() : BigDecimal.ZERO;

            double pct = 0.0;
            if (limite.compareTo(BigDecimal.ZERO) > 0) {
                pct = gastado.doubleValue() / limite.doubleValue() * 100.0;
            }
            String estado = pct < 60 ? "ok" : (pct < 85 ? "warn" : (pct < 100 ? "danger" : "over"));
            double pctBar = Math.min(pct, 100.0);

            BigDecimal gastadoConv = gastado.multiply(tasa).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal limiteConv = limite.multiply(tasa).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal gastadoEnCuentaConv = monedaService.convertir(
                    gastadoEnCuentaActiva.getOrDefault(p.getId(), BigDecimal.ZERO), moneda);

            vistas.add(new PresupuestoVista(p, gastado, limite,
                    gastadoConv, limiteConv, gastadoEnCuentaConv,
                    pct, pctBar, estado));
            totalGastadoPresupuestos = totalGastadoPresupuestos.add(gastado);
            totalLimite = totalLimite.add(limite);
        }

        BigDecimal totalLimiteConv = totalLimite.multiply(tasa).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal totalGastadoConv = totalGastadoPresupuestos.multiply(tasa).setScale(2, java.math.RoundingMode.HALF_UP);

        Map<LocalDate, List<Transaccion>> agrupadas = new LinkedHashMap<>();
        for (Transaccion t : conPresupuesto) {
            agrupadas.computeIfAbsent(t.getFecha(), k -> new ArrayList<>()).add(t);
        }

        model.addAttribute("presupuestos", presupuestos);
        model.addAttribute("vistas", vistas);
        model.addAttribute("totalLimiteConv", totalLimiteConv);
        model.addAttribute("totalGastadoConv", totalGastadoConv);
        model.addAttribute("totalExcedido", totalGastadoPresupuestos.compareTo(totalLimite) > 0);
        model.addAttribute("saldoTotal", saldoConvertido);
        model.addAttribute("formulario", formulario);
        model.addAttribute("categoriasSugeridas", CATEGORIAS_SUGERIDAS);
        model.addAttribute("agrupadas", agrupadas);
        model.addAttribute("anioActual", anio);
        model.addAttribute("mesActual", mes);
    }

    /**
     * DTO para la vista: presupuesto + cifras pre-calculadas (gastado global, gastado en
     * cuenta activa, %, estado, valores convertidos a moneda visible).
     */
    public static class PresupuestoVista {
        private final Presupuesto presupuesto;
        private final BigDecimal gastado;
        private final BigDecimal limite;
        private final BigDecimal gastadoConv;
        private final BigDecimal limiteConv;
        private final BigDecimal gastadoCuentaActivaConv;
        private final boolean hayGastoEnCuentaActiva;
        private final double pct;
        private final double pctBar;
        private final String estado;

        public PresupuestoVista(Presupuesto presupuesto, BigDecimal gastado, BigDecimal limite,
                                BigDecimal gastadoConv, BigDecimal limiteConv,
                                BigDecimal gastadoCuentaActivaConv,
                                double pct, double pctBar, String estado) {
            this.presupuesto = presupuesto;
            this.gastado = gastado;
            this.limite = limite;
            this.gastadoConv = gastadoConv;
            this.limiteConv = limiteConv;
            this.gastadoCuentaActivaConv = gastadoCuentaActivaConv;
            this.hayGastoEnCuentaActiva = gastadoCuentaActivaConv != null
                    && gastadoCuentaActivaConv.compareTo(BigDecimal.ZERO) > 0;
            this.pct = pct;
            this.pctBar = pctBar;
            this.estado = estado;
        }

        public Presupuesto getPresupuesto() { return presupuesto; }
        public BigDecimal getGastado() { return gastado; }
        public BigDecimal getLimite() { return limite; }
        public BigDecimal getGastadoConv() { return gastadoConv; }
        public BigDecimal getLimiteConv() { return limiteConv; }
        public BigDecimal getGastadoCuentaActivaConv() { return gastadoCuentaActivaConv; }
        public boolean isHayGastoEnCuentaActiva() { return hayGastoEnCuentaActiva; }
        public double getPct() { return pct; }
        public double getPctBar() { return pctBar; }
        public String getEstado() { return estado; }
        public boolean isOver() { return "over".equals(estado); }
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
