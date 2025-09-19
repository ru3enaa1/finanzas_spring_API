package com.app.finanzas.controller;

import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.CuentaService;
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
import java.util.List;

@Controller
@RequestMapping("/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;
    private final UsuarioService usuarioService;

    public CuentaController(CuentaService cuentaService, UsuarioService usuarioService) {
        this.cuentaService = cuentaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();
        prepararModeloCuentas(model, usuario, new Cuenta());
        return "cuentas/lista";
    }

    @PostMapping
    public String crear(@Valid Cuenta cuenta,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (bindingResult.hasErrors()) {
            prepararModeloCuentas(model, usuario, cuenta);
            return "cuentas/lista";
        }
        cuenta.setUsuario(usuario);
        cuentaService.crear(cuenta);
        redirectAttributes.addFlashAttribute("mensajeExito", "Cuenta creada correctamente");
        return "redirect:/cuentas";
    }

    @PostMapping("/{id}/actualizar")
    public String actualizar(@PathVariable Integer id,
                             @Valid Cuenta cuenta,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (bindingResult.hasErrors()) {
            cuenta.setId(id);
            prepararModeloCuentas(model, usuario, cuenta);
            return "cuentas/lista";
        }
        cuenta.setId(id);
        cuenta.setUsuario(usuario);
        cuentaService.actualizar(cuenta);
        redirectAttributes.addFlashAttribute("mensajeExito", "Cuenta actualizada");
        return "redirect:/cuentas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        cuentaService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Cuenta eliminada");
        return "redirect:/cuentas";
    }

    @PostMapping("/{id}/saldo")
    public String ajustarSaldo(@PathVariable Integer id,
                                @RequestParam BigDecimal saldo,
                                RedirectAttributes redirectAttributes) {
        cuentaService.buscarPorId(id).ifPresent(cuenta -> cuentaService.ajustarSaldo(cuenta, saldo));
        redirectAttributes.addFlashAttribute("mensajeExito", "Saldo ajustado");
        return "redirect:/cuentas";
    }

    private void prepararModeloCuentas(Model model, Usuario usuario, Cuenta cuenta) {
        List<Cuenta> cuentas = cuentaService.listarPorUsuario(usuario);
        BigDecimal saldoTotal = cuentas.stream()
                .map(Cuenta::getSaldo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("cuentas", cuentas);
        model.addAttribute("cuenta", cuenta);
        model.addAttribute("saldoTotal", saldoTotal);
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
