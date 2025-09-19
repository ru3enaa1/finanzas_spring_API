package com.app.finanzas.repository;

import com.app.finanzas.entity.RegistroGastoFijo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistroGastoFijoRepository extends JpaRepository<RegistroGastoFijo, Integer> {

    Optional<RegistroGastoFijo> findByGastoFijo_IdAndAnioAndMes(Integer gastoFijoId, Integer anio, Integer mes);

    List<RegistroGastoFijo> findByGastoFijo_Id(Integer gastoFijoId);
}
