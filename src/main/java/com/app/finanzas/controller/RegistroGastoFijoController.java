package com.app.finanzas.controller;

import com.app.finanzas.entity.RegistroGastoFijo;
import com.app.finanzas.service.RegistroGastoFijoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/registros-gastos-fijos")
public class RegistroGastoFijoController {

    private final RegistroGastoFijoService registroGastoFijoService;

    public RegistroGastoFijoController(RegistroGastoFijoService registroGastoFijoService) {
        this.registroGastoFijoService = registroGastoFijoService;
    }

    @GetMapping("/gasto/{gastoId}")
    public ResponseEntity<List<RegistroGastoFijo>> listarPorGasto(@PathVariable Integer gastoId) {
        return ResponseEntity.ok(registroGastoFijoService.listarPorGasto(gastoId));
    }

    @PostMapping
    public ResponseEntity<RegistroGastoFijo> guardar(@RequestBody RegistroGastoFijo registro) {
        return ResponseEntity.ok(registroGastoFijoService.guardar(registro));
    }
}
