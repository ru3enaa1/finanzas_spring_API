package com.app.finanzas.repository;

import com.app.finanzas.entity.Transaccion;
import com.app.finanzas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Integer> {

    List<Transaccion> findByCuenta_IdOrderByFechaDesc(Integer cuentaId);

    List<Transaccion> findTop10ByCuenta_UsuarioOrderByFechaDesc(Usuario usuario);

    List<Transaccion> findByCuenta_UsuarioOrderByFechaDesc(Usuario usuario);
}
