package com.app.finanzas.repository;

import com.app.finanzas.entity.Presupuesto;
import com.app.finanzas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Integer> {

    List<Presupuesto> findByUsuario(Usuario usuario);

    List<Presupuesto> findByUsuarioAndActivoTrue(Usuario usuario);

    Optional<Presupuesto> findByIdAndUsuario(Integer id, Usuario usuario);
}
