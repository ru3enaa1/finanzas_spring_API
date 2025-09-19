package com.app.finanzas.repository;

import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CuentaRepository extends JpaRepository<Cuenta, Integer> {

    List<Cuenta> findByUsuario(Usuario usuario);
}
