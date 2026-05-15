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

    @EntityGraph(attributePaths = {"cuenta", "presupuesto", "fondo"})
    List<Transaccion> findByCuenta_IdOrderByFechaDesc(Integer cuentaId);

    @EntityGraph(attributePaths = {"cuenta", "presupuesto", "fondo"})
    List<Transaccion> findTop10ByCuenta_UsuarioOrderByFechaDesc(Usuario usuario);

    @EntityGraph(attributePaths = {"cuenta", "presupuesto", "fondo"})
    List<Transaccion> findTop10ByCuenta_IdOrderByFechaDesc(Integer cuentaId);

    @EntityGraph(attributePaths = {"cuenta", "presupuesto", "fondo"})
    List<Transaccion> findByCuenta_UsuarioOrderByFechaDesc(Usuario usuario);

    @EntityGraph(attributePaths = {"cuenta", "presupuesto", "fondo"})
    Optional<Transaccion> findByIdAndCuenta_Usuario(Integer id, Usuario usuario);

    /**
     * Suma neta de transacciones de una cuenta directamente en BD.
     * Evita conflictos de session durante el recalculo dentro de una transaccion.
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN t.tipo = com.app.finanzas.entity.TipoTransaccion.INGRESO " +
           "THEN t.monto ELSE -t.monto END), 0) " +
           "FROM Transaccion t WHERE t.cuenta.id = :cuentaId")
    BigDecimal sumarDeltaPorCuenta(@Param("cuentaId") Integer cuentaId);

    /**
     * Total acumulado aportado a un fondo (suma de todos los gastos vinculados).
     */
    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t " +
           "WHERE t.fondo.id = :fondoId " +
           "AND t.tipo = com.app.finanzas.entity.TipoTransaccion.GASTO")
    BigDecimal sumarAportadoPorFondo(@Param("fondoId") Integer fondoId);

    /**
     * Total aportado a un fondo SOLO desde una cuenta (contexto dual estilo YNAB).
     */
    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM Transaccion t " +
           "WHERE t.fondo.id = :fondoId AND t.cuenta.id = :cuentaId " +
           "AND t.tipo = com.app.finanzas.entity.TipoTransaccion.GASTO")
    BigDecimal sumarAportadoPorFondoYCuenta(@Param("fondoId") Integer fondoId,
                                             @Param("cuentaId") Integer cuentaId);

    /**
     * Desglose de gasto del MES ACTUAL por cuenta para todos los presupuestos del usuario.
     * Retorna [presupuestoId, cuentaId, cuentaNombre, sumMonto].
     * Usado para barras segmentadas por cuenta.
     */
    @Query("SELECT t.presupuesto.id, t.cuenta.id, t.cuenta.nombre, SUM(t.monto) " +
           "FROM Transaccion t " +
           "WHERE t.presupuesto IS NOT NULL " +
           "AND t.cuenta.usuario = :usuario " +
           "AND t.tipo = com.app.finanzas.entity.TipoTransaccion.GASTO " +
           "AND t.fecha BETWEEN :inicio AND :fin " +
           "GROUP BY t.presupuesto.id, t.cuenta.id, t.cuenta.nombre " +
           "ORDER BY SUM(t.monto) DESC")
    List<Object[]> desglosePorCuentaPorPresupuestoEnRango(
            @Param("usuario") Usuario usuario,
            @Param("inicio") java.time.LocalDate inicio,
            @Param("fin") java.time.LocalDate fin);

    /**
     * Desglose acumulado de aportes por cuenta para todos los fondos del usuario (sin rango temporal).
     * Retorna [fondoId, cuentaId, cuentaNombre, sumMonto].
     */
    @Query("SELECT t.fondo.id, t.cuenta.id, t.cuenta.nombre, SUM(t.monto) " +
           "FROM Transaccion t " +
           "WHERE t.fondo IS NOT NULL " +
           "AND t.cuenta.usuario = :usuario " +
           "AND t.tipo = com.app.finanzas.entity.TipoTransaccion.GASTO " +
           "GROUP BY t.fondo.id, t.cuenta.id, t.cuenta.nombre " +
           "ORDER BY SUM(t.monto) DESC")
    List<Object[]> desglosePorCuentaPorFondo(@Param("usuario") Usuario usuario);
}
