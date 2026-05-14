package com.app.finanzas.repository;

import com.app.finanzas.entity.Alerta;
import com.app.finanzas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Integer> {

    List<Alerta> findTop5ByUsuarioOrderByFechaHoraDesc(Usuario usuario);

    boolean existsByUsuarioAndDescripcion(Usuario usuario, String descripcion);
}

