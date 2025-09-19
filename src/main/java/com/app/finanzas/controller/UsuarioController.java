package com.app.finanzas.controller;

import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registrar(@Valid Usuario usuario,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuario);
            return "auth/register";
        }
        if (usuarioService.existeCorreo(usuario.getCorreo())) {
            bindingResult.rejectValue("correo", "usuario.correo.duplicado", "El correo ya está registrado");
            model.addAttribute("usuario", usuario);
            return "auth/register";
        }
        usuarioService.registrar(usuario);
        redirectAttributes.addFlashAttribute("mensajeExito", "Registro exitoso, inicia sesión");
        return "redirect:/auth/login";
    }
}
