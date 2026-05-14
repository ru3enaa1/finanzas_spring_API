package com.app.finanzas.service;

import com.app.finanzas.entity.Alerta;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.AlertaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public AlertaService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    public Alerta registrar(Usuario usuario, String descripcion) {
        if (alertaRepository.existsByUsuarioAndDescripcion(usuario, descripcion)) {
            return null;
        }
        Alerta alerta = new Alerta();
        alerta.setUsuario(usuario);
        alerta.setDescripcion(descripcion);
        alerta.setFechaHora(LocalDateTime.now());
        return alertaRepository.save(alerta);
    }

    public List<Alerta> listarRecientes(Usuario usuario) {
        return alertaRepository.findTop5ByUsuarioOrderByFechaHoraDesc(usuario);
    }
}

