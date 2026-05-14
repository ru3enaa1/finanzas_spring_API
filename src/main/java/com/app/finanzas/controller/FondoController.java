package com.app.finanzas.controller;

import com.app.finanzas.dto.FondoResumen;
import com.app.finanzas.dto.RegistroFondoForm;
import com.app.finanzas.entity.Fondo;
import com.app.finanzas.entity.RegistroFondo;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.FondoService;
import com.app.finanzas.service.UsuarioService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/fondos")
public class FondoController {

    private final FondoService fondoService;
    private final UsuarioService usuarioService;

    public FondoController(FondoService fondoService, UsuarioService usuarioService) {
        this.fondoService = fondoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (!model.containsAttribute("fondo")) {
            model.addAttribute("fondo", new Fondo());
        }
        if (!model.containsAttribute("aporteForm")) {
            model.addAttribute("aporteForm", crearFormularioAportePorDefecto());
        }
        prepararModeloFondos(model, usuario);
        return "fondos/lista";
    }

    @PostMapping
    public String crear(@Valid Fondo fondo,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (fondoService.existeNombreParaUsuario(usuario, fondo.getNombre())) {
            bindingResult.rejectValue("nombre", "fondo.nombre.duplicado", "Ya existe un fondo con ese nombre");
        }
        if (bindingResult.hasErrors()) {
            prepararModeloFondos(model, usuario);
            model.addAttribute("aporteForm", crearFormularioAportePorDefecto());
            return "fondos/lista";
        }
        fondo.setUsuario(usuario);
        fondoService.crear(fondo);
        redirectAttributes.addFlashAttribute("mensajeExito", "Fondo creado correctamente");
        return "redirect:/fondos";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizar(@PathVariable Integer id,
                             @Valid Fondo fondo,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Fondo existente = fondoService.buscarPorIdYUsuario(id, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Fondo no encontrado"));
        if (!existente.getNombre().equalsIgnoreCase(fondo.getNombre())
                && fondoService.existeNombreParaUsuario(usuario, fondo.getNombre())) {
            bindingResult.rejectValue("nombre", "fondo.nombre.duplicado", "Ya existe un fondo con ese nombre");
        }
        if (bindingResult.hasErrors()) {
            fondo.setId(id);
            prepararModeloFondos(model, usuario);
            model.addAttribute("fondo", fondo);
            model.addAttribute("aporteForm", crearFormularioAportePorDefecto());
            return "fondos/lista";
        }
        fondo.setId(id);
        fondo.setUsuario(usuario);
        fondoService.actualizar(fondo);
        redirectAttributes.addFlashAttribute("mensajeExito", "Fondo actualizado");
        return "redirect:/fondos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Fondo fondo = fondoService.buscarPorIdYUsuario(id, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Fondo no encontrado"));
        fondoService.eliminar(fondo);
        redirectAttributes.addFlashAttribute("mensajeExito", "Fondo eliminado");
        return "redirect:/fondos";
    }

    @PostMapping("/{id}/aportes")
    public String registrarAporte(@PathVariable Integer id,
                                   @Valid RegistroFondoForm aporteForm,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();
        Fondo fondo = fondoService.buscarPorIdYUsuario(id, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Fondo no encontrado"));

        if (bindingResult.hasErrors()) {
            prepararModeloFondos(model, usuario);
            model.addAttribute("fondo", new Fondo());
            model.addAttribute("aporteForm", aporteForm);
            return "fondos/lista";
        }
        fondoService.registrarAporte(fondo, aporteForm.getAnio(), aporteForm.getMes(), aporteForm.getMonto());
        redirectAttributes.addFlashAttribute("mensajeExito", "Aporte registrado");
        return "redirect:/fondos";
    }

    private void prepararModeloFondos(Model model, Usuario usuario) {
        List<Fondo> fondos = fondoService.listarPorUsuario(usuario);
        YearMonth periodoActual = YearMonth.now();
        int anioActual = periodoActual.getYear();

        List<FondoResumen> resumenes = fondos.stream()
                .map(fondo -> new FondoResumen(
                        fondo.getId(),
                        fondo.getNombre(),
                        fondo.getMontoAnual(),
                        fondoService.calcularTotalAportado(fondo, anioActual),
                        fondoService.calcularPorcentajeAvance(fondo, anioActual),
                        anioActual
                ))
                .collect(Collectors.toList());

        Map<Integer, List<RegistroFondo>> registrosPorFondo = new LinkedHashMap<>();
        for (Fondo fondo : fondos) {
            registrosPorFondo.put(fondo.getId(), fondoService.listarRegistros(fondo));
        }

        model.addAttribute("fondosResumen", resumenes);
        model.addAttribute("registrosPorFondo", registrosPorFondo);
        model.addAttribute("anioActual", anioActual);
        model.addAttribute("usuarioActual", usuario.getNombre() != null ? usuario.getNombre() : usuario.getCorreo());
    }

    private RegistroFondoForm crearFormularioAportePorDefecto() {
        RegistroFondoForm formulario = new RegistroFondoForm();
        YearMonth ahora = YearMonth.now();
        formulario.setAnio(ahora.getYear());
        formulario.setMes(ahora.getMonthValue());
        return formulario;
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

