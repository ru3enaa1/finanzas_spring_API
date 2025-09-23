package com.app.finanzas.controller.api;

import com.app.finanzas.dto.auth.AuthResponse;
import com.app.finanzas.dto.auth.LoginRequest;
import com.app.finanzas.dto.auth.RegisterRequest;
import com.app.finanzas.dto.auth.UsuarioResumen;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST para registrar usuarios y validar el inicio de sesion desde clientes externos.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UsuarioService usuarioService;

    public AuthRestController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (usuarioService.existeCorreo(request.getCorreo())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(AuthResponse.error("El correo ya esta registrado"));
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setCorreo(request.getCorreo());
        usuario.setContrasenaPlano(request.getContrasena());

        Usuario registrado = usuarioService.registrar(usuario);
        UsuarioResumen resumen = new UsuarioResumen(registrado.getId(), registrado.getNombre(), registrado.getCorreo());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.success("Registro exitoso", resumen));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return usuarioService.autenticar(request.getCorreo(), request.getContrasena())
                .map(usuario -> {
                    UsuarioResumen resumen = new UsuarioResumen(usuario.getId(), usuario.getNombre(), usuario.getCorreo());
                    return ResponseEntity.ok(AuthResponse.success("Autenticacion satisfactoria", resumen));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AuthResponse.error("Error en la autenticacion")));
    }
}
