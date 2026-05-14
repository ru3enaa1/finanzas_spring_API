package com.app.finanzas.repository;

import com.app.finanzas.entity.Transaccion;
import com.app.finanzas.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransaccionRepository extends JpaRepository<Transaccion, Integer> {

    @EntityGraph(attributePaths = {"cuenta", "presupuesto"})
    List<Transaccion> findByCuenta_IdOrderByFechaDesc(Integer cuentaId);

    @EntityGraph(attributePaths = {"cuenta", "presupuesto"})
    List<Transaccion> findTop10ByCuenta_UsuarioOrderByFechaDesc(Usuario usuario);

    @EntityGraph(attributePaths = {"cuenta", "presupuesto"})
    List<Transaccion> findTop10ByCuenta_IdOrderByFechaDesc(Integer cuentaId);

    @EntityGraph(attributePaths = {"cuenta", "presupuesto"})
    List<Transaccion> findByCuenta_UsuarioOrderByFechaDesc(Usuario usuario);

    @EntityGraph(attributePaths = {"cuenta", "presupuesto"})
    Optional<Transaccion> findByIdAndCuenta_Usuario(Integer id, Usuario usuario);

    /**
     * Calcula la suma neta de transacciones de una cuenta directamente en BD.
     * Mas eficiente que cargar todas las entidades en memoria, y evita conflictos
     * de session.assertion durante el recalculo dentro de una transaccion.
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN t.tipo = com.app.finanzas.entity.TipoTransaccion.INGRESO " +
           "THEN t.monto ELSE -t.monto END), 0) " +
           "FROM Transaccion t WHERE t.cuenta.id = :cuentaId")
    BigDecimal sumarDeltaPorCuenta(@Param("cuentaId") Integer cuentaId);
}
