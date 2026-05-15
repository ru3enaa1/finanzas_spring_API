package com.app.finanzas.repository;

import com.app.finanzas.entity.Fondo;
import com.app.finanzas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FondoRepository extends JpaRepository<Fondo, Integer> {

    List<Fondo> findByUsuarioOrderByNombreAsc(Usuario usuario);

    List<Fondo> findByUsuarioAndActivoTrueOrderByNombreAsc(Usuario usuario);

    Optional<Fondo> findByIdAndUsuario(Integer id, Usuario usuario);

    boolean existsByUsuarioAndNombreIgnoreCase(Usuario usuario, String nombre);
}
