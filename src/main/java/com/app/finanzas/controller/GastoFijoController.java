package com.app.finanzas.controller;

import com.app.finanzas.entity.GastoFijo;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.GastoFijoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/gastos-fijos")
public class GastoFijoController {

    private final GastoFijoService gastoFijoService;
    private final UsuarioService usuarioService;

    public GastoFijoController(GastoFijoService gastoFijoService, UsuarioService usuarioService) {
        this.gastoFijoService = gastoFijoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();
        prepararModeloGastos(model, usuario, new GastoFijo());
        return "gastos/lista";
    }

    @PostMapping
    public String crear(@Valid GastoFijo gastoFijo,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (bindingResult.hasErrors()) {
            prepararModeloGastos(model, usuario, gastoFijo);
            return "gastos/lista";
        }
        gastoFijo.setUsuario(usuario);
        gastoFijoService.registrar(gastoFijo);
        redirectAttributes.addFlashAttribute("mensajeExito", "Gasto fijo registrado");
        return "redirect:/gastos-fijos";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizar(@PathVariable Integer id,
                             @Valid GastoFijo gastoFijo,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (bindingResult.hasErrors()) {
            gastoFijo.setId(id);
            prepararModeloGastos(model, usuario, gastoFijo);
            return "gastos/lista";
        }
        gastoFijo.setId(id);
        gastoFijo.setUsuario(usuario);
        gastoFijoService.actualizar(gastoFijo);
        redirectAttributes.addFlashAttribute("mensajeExito", "Gasto fijo actualizado");
        return "redirect:/gastos-fijos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        gastoFijoService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Gasto fijo eliminado");
        return "redirect:/gastos-fijos";
    }

    @PostMapping("/{id}/registrar-pago")
    public String registrarPago(@PathVariable Integer id,
                                @RequestParam Integer anio,
                                @RequestParam Integer mes,
                                @RequestParam(required = false) BigDecimal montoPagado,
                                @RequestParam(defaultValue = "false") boolean pagado,
                                RedirectAttributes redirectAttributes) {
        GastoFijo gastoFijo = gastoFijoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Gasto fijo no encontrado"));
        gastoFijoService.registrarPago(gastoFijo, anio, mes, montoPagado, pagado,
                pagado ? LocalDate.now() : null);
        redirectAttributes.addFlashAttribute("mensajeExito", "Registro actualizado");
        return "redirect:/gastos-fijos/" + id + "/registros";
    }

    @GetMapping("/{id}/registros")
    public String verDetalle(@PathVariable Integer id, Model model) {
        GastoFijo gastoFijo = gastoFijoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Gasto fijo no encontrado"));
        Usuario usuario = obtenerUsuarioAutenticado();
        model.addAttribute("gasto", gastoFijo);
        model.addAttribute("registros", gastoFijoService.listarRegistros(id));
        model.addAttribute("usuarioActual", usuario.getNombre() != null ? usuario.getNombre() : usuario.getCorreo());
        return "gastos/detalle";
    }

    private void prepararModeloGastos(Model model, Usuario usuario, GastoFijo gasto) {
        List<GastoFijo> gastos = gastoFijoService.listarPorUsuario(usuario);
        model.addAttribute("gastos", gastos);
        model.addAttribute("gasto", gasto);
        model.addAttribute("usuarioActual", usuario.getNombre() != null ? usuario.getNombre() : usuario.getCorreo());
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
