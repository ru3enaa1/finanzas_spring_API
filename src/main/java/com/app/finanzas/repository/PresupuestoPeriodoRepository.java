package com.app.finanzas.repository;

import com.app.finanzas.entity.PresupuestoPeriodo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PresupuestoPeriodoRepository extends JpaRepository<PresupuestoPeriodo, Integer> {

    Optional<PresupuestoPeriodo> findByPresupuesto_IdAndAnioAndMes(Integer presupuestoId, Integer anio, Integer mes);

    List<PresupuestoPeriodo> findByPresupuesto_Id(Integer presupuestoId);

    List<PresupuestoPeriodo> findByPresupuesto_Usuario_IdAndAnioAndMes(Integer usuarioId, Integer anio, Integer mes);
}
