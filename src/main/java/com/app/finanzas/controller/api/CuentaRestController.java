package com.app.finanzas.controller.api;

import com.app.finanzas.dto.api.CuentaRequest;
import com.app.finanzas.dto.api.CuentaResponse;
import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.CuentaService;
import com.app.finanzas.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaRestController {

    private final CuentaService cuentaService;
    private final UsuarioService usuarioService;

    public CuentaRestController(CuentaService cuentaService, UsuarioService usuarioService) {
        this.cuentaService = cuentaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listar() {
        Usuario usuario = obtenerUsuarioAutenticado();
        List<CuentaResponse> cuentas = cuentaService.listarPorUsuario(usuario).stream()
                .map(this::mapearCuenta)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cuentas);
    }

    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaRequest request) {
        Usuario usuario = obtenerUsuarioAutenticado();
        if (request.saldo() != null && request.saldo().compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((CuentaResponse) null);
        }

        Cuenta cuenta = new Cuenta();
        cuenta.setTipo(request.tipo().trim());
        cuenta.setSaldo(request.saldo() != null ? request.saldo() : BigDecimal.ZERO);
        cuenta.setUsuario(usuario);
        Cuenta creada = cuentaService.crear(cuenta);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapearCuenta(creada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaResponse> actualizar(@PathVariable Integer id,
                                                     @Valid @RequestBody CuentaRequest request) {
        Usuario usuario = obtenerUsuarioAutenticado();
        return cuentaService.buscarPorIdYUsuario(id, usuario)
                .map(cuenta -> {
                    if (request.saldo() != null && request.saldo().compareTo(BigDecimal.ZERO) < 0) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body((CuentaResponse) null);
                    }
                    cuenta.setTipo(request.tipo().trim());
                    cuenta.setSaldo(request.saldo() != null ? request.saldo() : BigDecimal.ZERO);
                    Cuenta actualizada = cuentaService.actualizar(cuenta);
                    return ResponseEntity.ok(mapearCuenta(actualizada));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body((CuentaResponse) null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Usuario usuario = obtenerUsuarioAutenticado();
        return cuentaService.buscarPorIdYUsuario(id, usuario)
                .map(cuenta -> {
                    cuentaService.eliminar(cuenta.getId());
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).<Void>build());
    }

    private CuentaResponse mapearCuenta(Cuenta cuenta) {
        return new CuentaResponse(cuenta.getId(), cuenta.getTipo(), cuenta.getSaldo());
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
