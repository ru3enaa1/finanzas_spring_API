package com.app.finanzas.repository;

import com.app.finanzas.entity.GastoFijo;
import com.app.finanzas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GastoFijoRepository extends JpaRepository<GastoFijo, Integer> {

    List<GastoFijo> findByUsuario(Usuario usuario);

    List<GastoFijo> findByUsuarioAndActivoTrue(Usuario usuario);
}
