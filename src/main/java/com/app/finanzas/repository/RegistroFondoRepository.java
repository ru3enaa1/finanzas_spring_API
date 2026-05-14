package com.app.finanzas.repository;

import com.app.finanzas.entity.RegistroFondo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RegistroFondoRepository extends JpaRepository<RegistroFondo, Integer> {

    List<RegistroFondo> findByFondo_IdOrderByAnioDescMesDesc(Integer fondoId);

    Optional<RegistroFondo> findByFondo_IdAndAnioAndMes(Integer fondoId, Integer anio, Integer mes);

    @Query("SELECT COALESCE(SUM(r.montoAportado), 0) FROM RegistroFondo r WHERE r.fondo.id = :fondoId AND r.anio = :anio")
    BigDecimal totalAportadoEnAnio(@Param("fondoId") Integer fondoId, @Param("anio") Integer anio);
}
