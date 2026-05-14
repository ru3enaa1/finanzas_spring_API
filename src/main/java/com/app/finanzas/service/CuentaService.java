package com.app.finanzas.service;

import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.CuentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CuentaService {

    private final CuentaRepository cuentaRepository;

    public CuentaService(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    public Cuenta crear(Cuenta cuenta) {
        return cuentaRepository.save(cuenta);
    }

    public Cuenta actualizar(Cuenta cuenta) {
        return cuentaRepository.save(cuenta);
    }

    public void eliminar(Integer id) {
        cuentaRepository.deleteById(id);
    }

    public List<Cuenta> listarPorUsuario(Usuario usuario) {
        return cuentaRepository.findByUsuario(usuario);
    }

    public Optional<Cuenta> buscarPorId(Integer id) {
        return cuentaRepository.findById(id);
    }

    public Optional<Cuenta> buscarPorIdYUsuario(Integer id, Usuario usuario) {
        return cuentaRepository.findByIdAndUsuario(id, usuario);
    }

    public void ajustarSaldo(Cuenta cuenta, BigDecimal nuevoSaldo) {
        // Ajustar saldoInicial para mantener la invariante saldo = saldoInicial + sum(transacciones)
        BigDecimal diff = nuevoSaldo.subtract(cuenta.getSaldo());
        cuenta.setSaldoInicial(cuenta.getSaldoInicial().add(diff));
        cuenta.setSaldo(nuevoSaldo);
        cuentaRepository.save(cuenta);
    }
}
