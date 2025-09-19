package com.app.finanzas.controller;

import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.TipoTransaccion;
import com.app.finanzas.entity.Transaccion;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.CuentaService;
import com.app.finanzas.service.TransaccionService;
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

import java.time.LocalDate;
import java.util.Arrays;

@Controller
@RequestMapping("/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final CuentaService cuentaService;
    private final UsuarioService usuarioService;

    public TransaccionController(TransaccionService transaccionService,
                                 CuentaService cuentaService,
                                 UsuarioService usuarioService) {
        this.transaccionService = transaccionService;
        this.cuentaService = cuentaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();
        prepararModeloTransacciones(model, usuario, new Transaccion());
        return "transacciones/lista";
    }

    @PostMapping
    public String crear(@Valid Transaccion transaccion,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (bindingResult.hasErrors()) {
            prepararModeloTransacciones(model, usuario, transaccion);
            return "transacciones/lista";
        }
        Cuenta cuenta = cuentaService.buscarPorId(transaccion.getCuenta().getId())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada"));
        transaccion.setCuenta(cuenta);
        transaccionService.registrar(transaccion);
        redirectAttributes.addFlashAttribute("mensajeExito", "Transaccion registrada");
        return "redirect:/transacciones";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        transaccionService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Transaccion eliminada");
        return "redirect:/transacciones";
    }

    private void prepararModeloTransacciones(Model model, Usuario usuario, Transaccion transaccion) {
        if (transaccion.getFecha() == null) {
            transaccion.setFecha(LocalDate.now());
        }
        model.addAttribute("cuentas", cuentaService.listarPorUsuario(usuario));
        model.addAttribute("tipos", Arrays.asList(TipoTransaccion.values()));
        model.addAttribute("transaccion", transaccion);
        model.addAttribute("transacciones", transaccionService.listarPorUsuario(usuario));
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
