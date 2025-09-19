package com.app.finanzas.service;

import com.app.finanzas.entity.RegistroGastoFijo;
import com.app.finanzas.repository.RegistroGastoFijoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RegistroGastoFijoService {

    private final RegistroGastoFijoRepository registroGastoFijoRepository;

    public RegistroGastoFijoService(RegistroGastoFijoRepository registroGastoFijoRepository) {
        this.registroGastoFijoRepository = registroGastoFijoRepository;
    }

    public RegistroGastoFijo guardar(RegistroGastoFijo registro) {
        return registroGastoFijoRepository.save(registro);
    }

    public Optional<RegistroGastoFijo> buscarPorId(Integer id) {
        return registroGastoFijoRepository.findById(id);
    }

    public Optional<RegistroGastoFijo> buscarPorGastoYPeriodo(Integer gastoFijoId, Integer anio, Integer mes) {
        return registroGastoFijoRepository.findByGastoFijo_IdAndAnioAndMes(gastoFijoId, anio, mes);
    }

    public List<RegistroGastoFijo> listarPorGasto(Integer gastoFijoId) {
        return registroGastoFijoRepository.findByGastoFijo_Id(gastoFijoId);
    }

    public void eliminar(Integer id) {
        registroGastoFijoRepository.deleteById(id);
    }
}
