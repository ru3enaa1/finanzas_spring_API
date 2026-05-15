package com.app.finanzas.controller;

import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.Fondo;
import com.app.finanzas.entity.TipoTransaccion;
import com.app.finanzas.entity.Transaccion;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.TransaccionRepository;
import com.app.finanzas.service.CuentaService;
import com.app.finanzas.service.FondoService;
import com.app.finanzas.service.MonedaService;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/fondos")
public class FondoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FondoController.class);

    private static final List<String> CATEGORIAS_SUGERIDAS = List.of(
            "Vacaciones", "Vehículo", "Casa propia", "Educación",
            "Emergencias", "Inversiones", "Boda", "Tecnología", "Otros"
    );

    private final FondoService fondoService;
    private final TransaccionService transaccionService;
    private final TransaccionRepository transaccionRepository;
    private final CuentaService cuentaService;
    private final UsuarioService usuarioService;
    private final MonedaService monedaService;

    public FondoController(FondoService fondoService,
                           TransaccionService transaccionService,
                           TransaccionRepository transaccionRepository,
                           CuentaService cuentaService,
                           UsuarioService usuarioService,
                           MonedaService monedaService) {
        this.fondoService = fondoService;
        this.transaccionService = transaccionService;
        this.transaccionRepository = transaccionRepository;
        this.cuentaService = cuentaService;
        this.usuarioService = usuarioService;
        this.monedaService = monedaService;
    }

    @GetMapping
    public String listar(Model model, HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Fondo nuevo = new Fondo();
        prepararModelo(model, usuario, nuevo, session);
        return "fondos/lista";
    }

    @PostMapping
    public String crear(@Valid Fondo fondo,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model,
                        HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado();

        if (fondoService.existeNombreParaUsuario(usuario, fondo.getNombre())) {
            bindingResult.rejectValue("nombre", "fondo.nombre.duplicado",
                    "Ya tienes una bolsa con ese nombre");
        }

        if (bindingResult.hasErrors()) {
            LOGGER.warn("Errores al crear fondo: {}", bindingResult.getAllErrors());
            prepararModelo(model, usuario, fondo, session);
            model.addAttribute("formConErrores", true);
            return "fondos/lista";
        }

        try {
            fondo.setUsuario(usuario);
            fondo.setActivo(true);
            fondoService.crear(fondo);
            redirectAttributes.addFlashAttribute("mensajeExito", "Bolsa de ahorro creada");
        } catch (RuntimeException ex) {
            LOGGER.error("Fallo al crear fondo", ex);
            redirectAttributes.addFlashAttribute("mensajeError",
                    "No se pudo crear la bolsa: " + ex.getMessage());
        }
        return "redirect:/fondos";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizar(@PathVariable Integer id,
                             @Valid Fondo fondo,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (bindingResult.hasErrors()) {
            fondo.setId(id);
            prepararModelo(model, usuario, fondo, session);
            model.addAttribute("formConErrores", true);
            return "fondos/lista";
        }
        fondoService.buscarPorIdYUsuario(id, usuario).ifPresent(existente -> {
            existente.setNombre(fondo.getNombre());
            existente.setMontoAnual(fondo.getMontoAnual());
            if (fondo.getColor() != null && !fondo.getColor().isBlank()) {
                existente.setColor(fondo.getColor());
            }
            existente.setDescripcion(fondo.getDescripcion());
            fondoService.actualizar(existente);
        });
        redirectAttributes.addFlashAttribute("mensajeExito", "Bolsa actualizada");
        return "redirect:/fondos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuarioAutenticado();
        // Archivar: preservamos aportes historicos como transacciones reales
        fondoService.buscarPorIdYUsuario(id, usuario).ifPresent(f -> {
            f.setActivo(false);
            fondoService.actualizar(f);
        });
        redirectAttributes.addFlashAttribute("mensajeExito", "Bolsa archivada");
        return "redirect:/fondos";
    }

    private void prepararModelo(Model model, Usuario usuario, Fondo formulario, HttpSession session) {
        List<Fondo> fondos = fondoService.listarActivosPorUsuario(usuario);

        String moneda = monedaService.normalizar((String) session.getAttribute("monedaPreferida"));
        BigDecimal tasa = monedaService.tasa(moneda);

        // Cuenta activa + saldo
        Integer activaId = (Integer) session.getAttribute("cuentaActivaId");
        BigDecimal saldoCuentaActiva = cuentaService.listarPorUsuario(usuario).stream()
                .filter(c -> c.getId().equals(activaId))
                .map(Cuenta::getSaldo)
                .findFirst()
                .orElse(BigDecimal.ZERO);
        BigDecimal saldoConvertido = monedaService.convertir(saldoCuentaActiva, moneda);

        // Transacciones de aporte de la cuenta activa (para historial filtrable)
        List<Transaccion> aportesCuentaActiva = activaId != null
                ? transaccionService.listarPorCuenta(activaId).stream()
                        .filter(t -> t.getFondo() != null)
                        .filter(t -> t.getTipo() == TipoTransaccion.GASTO)
                        .toList()
                : new ArrayList<>();

        // Aporte de la cuenta activa por fondo (contexto dual estilo YNAB)
        Map<Integer, BigDecimal> aportadoEnCuentaActiva = new HashMap<>();
        for (Transaccion t : aportesCuentaActiva) {
            aportadoEnCuentaActiva.merge(t.getFondo().getId(), t.getMonto(), BigDecimal::add);
        }

        // Pre-calcular vista de cada fondo
        List<FondoVista> vistas = new ArrayList<>();
        BigDecimal totalMeta = BigDecimal.ZERO;
        BigDecimal totalAportado = BigDecimal.ZERO;

        for (Fondo f : fondos) {
            BigDecimal aportado = transaccionRepository.sumarAportadoPorFondo(f.getId());
            if (aportado == null) aportado = BigDecimal.ZERO;
            BigDecimal meta = f.getMontoAnual() != null ? f.getMontoAnual() : BigDecimal.ZERO;

            double pct = 0.0;
            if (meta.compareTo(BigDecimal.ZERO) > 0) {
                pct = aportado.doubleValue() / meta.doubleValue() * 100.0;
            }
            double pctBar = Math.min(pct, 100.0);
            boolean metaAlcanzada = pct >= 100.0;

            BigDecimal aportadoConv = aportado.multiply(tasa).setScale(2, RoundingMode.HALF_UP);
            BigDecimal metaConv = meta.multiply(tasa).setScale(2, RoundingMode.HALF_UP);
            BigDecimal aportadoEnCuentaConv = monedaService.convertir(
                    aportadoEnCuentaActiva.getOrDefault(f.getId(), BigDecimal.ZERO), moneda);

            vistas.add(new FondoVista(f, aportado, meta, aportadoConv, metaConv,
                    aportadoEnCuentaConv, pct, pctBar, metaAlcanzada));
            totalMeta = totalMeta.add(meta);
            totalAportado = totalAportado.add(aportado);
        }

        BigDecimal totalMetaConv = totalMeta.multiply(tasa).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAportadoConv = totalAportado.multiply(tasa).setScale(2, RoundingMode.HALF_UP);

        // Historial agrupado por fecha (solo aportes de la cuenta activa)
        Map<LocalDate, List<Transaccion>> agrupadas = new LinkedHashMap<>();
        for (Transaccion t : aportesCuentaActiva) {
            agrupadas.computeIfAbsent(t.getFecha(), k -> new ArrayList<>()).add(t);
        }

        model.addAttribute("fondos", fondos);
        model.addAttribute("vistas", vistas);
        model.addAttribute("totalMetaConv", totalMetaConv);
        model.addAttribute("totalAportadoConv", totalAportadoConv);
        model.addAttribute("saldoTotal", saldoConvertido);
        model.addAttribute("formulario", formulario);
        model.addAttribute("categoriasSugeridas", CATEGORIAS_SUGERIDAS);
        model.addAttribute("agrupadas", agrupadas);
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuario no autenticado");
        }
        return usuarioService.buscarPorCorreo(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
    }

    /**
     * DTO para la vista: fondo + cifras precalculadas (aportado global, en cuenta activa,
     * %, meta alcanzada, valores convertidos a moneda visible).
     */
    public static class FondoVista {
        private final Fondo fondo;
        private final BigDecimal aportado;
        private final BigDecimal meta;
        private final BigDecimal aportadoConv;
        private final BigDecimal metaConv;
        private final BigDecimal aportadoCuentaActivaConv;
        private final boolean hayAporteEnCuentaActiva;
        private final double pct;
        private final double pctBar;
        private final boolean metaAlcanzada;

        public FondoVista(Fondo fondo, BigDecimal aportado, BigDecimal meta,
                          BigDecimal aportadoConv, BigDecimal metaConv,
                          BigDecimal aportadoCuentaActivaConv,
                          double pct, double pctBar, boolean metaAlcanzada) {
            this.fondo = fondo;
            this.aportado = aportado;
            this.meta = meta;
            this.aportadoConv = aportadoConv;
            this.metaConv = metaConv;
            this.aportadoCuentaActivaConv = aportadoCuentaActivaConv;
            this.hayAporteEnCuentaActiva = aportadoCuentaActivaConv != null
                    && aportadoCuentaActivaConv.compareTo(BigDecimal.ZERO) > 0;
            this.pct = pct;
            this.pctBar = pctBar;
            this.metaAlcanzada = metaAlcanzada;
        }

        public Fondo getFondo() { return fondo; }
        public BigDecimal getAportado() { return aportado; }
        public BigDecimal getMeta() { return meta; }
        public BigDecimal getAportadoConv() { return aportadoConv; }
        public BigDecimal getMetaConv() { return metaConv; }
        public BigDecimal getAportadoCuentaActivaConv() { return aportadoCuentaActivaConv; }
        public boolean isHayAporteEnCuentaActiva() { return hayAporteEnCuentaActiva; }
        public double getPct() { return pct; }
        public double getPctBar() { return pctBar; }
        public boolean isMetaAlcanzada() { return metaAlcanzada; }
    }
}
