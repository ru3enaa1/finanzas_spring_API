package com.app.finanzas.service;

import com.app.finanzas.entity.TipoTransaccion;
import com.app.finanzas.entity.Transaccion;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.CuentaRepository;
import com.app.finanzas.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CuentaRepository cuentaRepository;

    public TransaccionService(TransaccionRepository transaccionRepository,
                              CuentaRepository cuentaRepository) {
        this.transaccionRepository = transaccionRepository;
        this.cuentaRepository = cuentaRepository;
    }

    public Transaccion registrar(Transaccion transaccion) {
        Transaccion guardada = transaccionRepository.save(transaccion);
        recalcularSaldoCuenta(guardada.getCuenta().getId());
        return guardada;
    }

    public Transaccion actualizar(Transaccion transaccion) {
        Transaccion actualizada = transaccionRepository.save(transaccion);
        recalcularSaldoCuenta(actualizada.getCuenta().getId());
        return actualizada;
    }

    public void eliminar(Integer id) {
        transaccionRepository.findById(id).ifPresent(transaccion -> {
            Integer cuentaId = transaccion.getCuenta().getId();
            transaccionRepository.delete(transaccion);
            recalcularSaldoCuenta(cuentaId);
        });
    }

    public Optional<Transaccion> buscarPorId(Integer id) {
        return transaccionRepository.findById(id);
    }

    public List<Transaccion> listarPorCuenta(Integer cuentaId) {
        return transaccionRepository.findByCuenta_IdOrderByFechaDesc(cuentaId);
    }

    public List<Transaccion> listarPorUsuario(Usuario usuario) {
        return transaccionRepository.findByCuenta_UsuarioOrderByFechaDesc(usuario);
    }

    public List<Transaccion> listarRecientesPorUsuario(Usuario usuario, int limite) {
        if (limite <= 0) {
            return Collections.emptyList();
        }
        List<Transaccion> recientes = transaccionRepository.findTop10ByCuenta_UsuarioOrderByFechaDesc(usuario);
        int toIndex = Math.min(limite, recientes.size());
        return recientes.subList(0, toIndex);
    }

    private void recalcularSaldoCuenta(Integer cuentaId) {
        cuentaRepository.findById(cuentaId).ifPresent(cuenta -> {
            List<Transaccion> transacciones = transaccionRepository.findByCuenta_IdOrderByFechaDesc(cuentaId);
            BigDecimal nuevoSaldo = transacciones.stream()
                    .map(trans -> trans.getTipo() == TipoTransaccion.INGRESO
                            ? trans.getMonto()
                            : trans.getMonto().negate())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            cuenta.setSaldo(nuevoSaldo);
            cuentaRepository.save(cuenta);
        });
    }
}
