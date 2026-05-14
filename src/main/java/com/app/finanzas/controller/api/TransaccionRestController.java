package com.app.finanzas.controller.api;

import com.app.finanzas.dto.api.TransaccionRequest;
import com.app.finanzas.dto.api.TransaccionResponse;
import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.Transaccion;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.CuentaService;
import com.app.finanzas.service.TransaccionService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TransaccionRestController {

    private final TransaccionService transaccionService;
    private final CuentaService cuentaService;
    private final UsuarioService usuarioService;

    public TransaccionRestController(TransaccionService transaccionService,
                                     CuentaService cuentaService,
                                     UsuarioService usuarioService) {
        this.transaccionService = transaccionService;
        this.cuentaService = cuentaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/transacciones")
    public ResponseEntity<List<TransaccionResponse>> listarTodas() {
        Usuario usuario = obtenerUsuarioAutenticado();
        List<TransaccionResponse> transacciones = transaccionService.listarPorUsuario(usuario).stream()
                .map(this::mapearTransaccion)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transacciones);
    }

    @GetMapping("/cuentas/{cuentaId}/transacciones")
    public ResponseEntity<List<TransaccionResponse>> listarPorCuenta(@PathVariable Integer cuentaId) {
        Usuario usuario = obtenerUsuarioAutenticado();
        return cuentaService.buscarPorIdYUsuario(cuentaId, usuario)
                .map(cuenta -> {
                    List<TransaccionResponse> transacciones = transaccionService.listarPorCuenta(cuenta.getId()).stream()
                            .map(this::mapearTransaccion)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(transacciones);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body((List<TransaccionResponse>) null));
    }

    @PostMapping("/transacciones")
    public ResponseEntity<TransaccionResponse> crear(@Valid @RequestBody TransaccionRequest request) {
        Usuario usuario = obtenerUsuarioAutenticado();
        return cuentaService.buscarPorIdYUsuario(request.cuentaId(), usuario)
                .map(cuenta -> {
                    Transaccion transaccion = new Transaccion();
                    transaccion.setCuenta(cuenta);
                    transaccion.setTipo(request.tipo());
                    transaccion.setCategoria(request.categoria().trim());
                    transaccion.setFecha(request.fecha());
                    transaccion.setMonto(request.monto());
                    String descripcion = request.descripcion();
                    transaccion.setDescripcion(descripcion != null && !descripcion.isBlank() ? descripcion.trim() : null);
                    transaccion.setFijo(request.fijo());

                    Transaccion creada = transaccionService.registrar(transaccion);
                    return ResponseEntity.status(HttpStatus.CREATED).body(mapearTransaccion(creada));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body((TransaccionResponse) null));
    }

    @DeleteMapping("/transacciones/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Usuario usuario = obtenerUsuarioAutenticado();
        return transaccionService.buscarPorIdYUsuario(id, usuario)
                .map(transaccion -> {
                    transaccionService.eliminar(transaccion.getId());
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    private TransaccionResponse mapearTransaccion(Transaccion transaccion) {
        return new TransaccionResponse(
                transaccion.getId(),
                transaccion.getCuenta().getId(),
                transaccion.getTipo(),
                transaccion.getCategoria(),
                transaccion.getFecha(),
                transaccion.getMonto(),
                transaccion.getDescripcion(),
                transaccion.isFijo()
        );
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

