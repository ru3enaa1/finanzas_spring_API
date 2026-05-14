package com.app.finanzas.service;

import com.app.finanzas.entity.Presupuesto;
import com.app.finanzas.entity.PresupuestoPeriodo;
import com.app.finanzas.entity.TipoTransaccion;
import com.app.finanzas.entity.Transaccion;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.CuentaRepository;
import com.app.finanzas.repository.PresupuestoPeriodoRepository;
import com.app.finanzas.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CuentaRepository cuentaRepository;
    private final PresupuestoPeriodoRepository presupuestoPeriodoRepository;

    public TransaccionService(TransaccionRepository transaccionRepository,
                              CuentaRepository cuentaRepository,
                              PresupuestoPeriodoRepository presupuestoPeriodoRepository) {
        this.transaccionRepository = transaccionRepository;
        this.cuentaRepository = cuentaRepository;
        this.presupuestoPeriodoRepository = presupuestoPeriodoRepository;
    }

    public Transaccion registrar(Transaccion transaccion) {
        if (transaccion.getFechaRegistro() == null) {
            transaccion.setFechaRegistro(LocalDateTime.now());
        }
        Transaccion guardada = transaccionRepository.save(transaccion);
        recalcularSaldoCuenta(guardada.getCuenta().getId());
        actualizarPeriodoPresupuesto(guardada, true);
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
            actualizarPeriodoPresupuesto(transaccion, false);
            transaccionRepository.delete(transaccion);
            recalcularSaldoCuenta(cuentaId);
        });
    }

    /**
     * Suma/resta del PresupuestoPeriodo cuando se crea o elimina una transaccion de tipo GASTO
     * asociada a un presupuesto. Solo gastos cuentan contra el presupuesto.
     * Implementado inline (sin delegar a PresupuestoService) para evitar problemas con
     * proxies @Lazy en contexto transaccional anidado.
     */
    private void actualizarPeriodoPresupuesto(Transaccion t, boolean sumar) {
        Presupuesto p = t.getPresupuesto();
        if (p == null || t.getTipo() != TipoTransaccion.GASTO || t.getFecha() == null) {
            return;
        }
        int anio = t.getFecha().getYear();
        int mes = t.getFecha().getMonthValue();
        BigDecimal delta = sumar ? t.getMonto() : t.getMonto().negate();

        PresupuestoPeriodo periodo = presupuestoPeriodoRepository
                .findByPresupuesto_IdAndAnioAndMes(p.getId(), anio, mes)
                .orElseGet(() -> {
                    PresupuestoPeriodo nuevo = new PresupuestoPeriodo();
                    nuevo.setPresupuesto(p);
                    nuevo.setAnio(anio);
                    nuevo.setMes(mes);
                    nuevo.setMontoLimiteSnapshot(p.getMontoEstimado());
                    nuevo.setMontoGastado(BigDecimal.ZERO);
                    return nuevo;
                });

        BigDecimal nuevoGastado = periodo.getMontoGastado().add(delta);
        if (nuevoGastado.compareTo(BigDecimal.ZERO) < 0) {
            nuevoGastado = BigDecimal.ZERO;
        }
        periodo.setMontoGastado(nuevoGastado);
        presupuestoPeriodoRepository.save(periodo);
    }

    public Optional<Transaccion> buscarPorId(Integer id) {
        return transaccionRepository.findById(id);
    }

    public Optional<Transaccion> buscarPorIdYUsuario(Integer id, Usuario usuario) {
        return transaccionRepository.findByIdAndCuenta_Usuario(id, usuario);
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

    public List<Transaccion> listarRecientesPorCuenta(Integer cuentaId, int limite) {
        if (limite <= 0 || cuentaId == null) {
            return Collections.emptyList();
        }
        List<Transaccion> recientes = transaccionRepository.findTop10ByCuenta_IdOrderByFechaDesc(cuentaId);
        int toIndex = Math.min(limite, recientes.size());
        return recientes.subList(0, toIndex);
    }

    private void recalcularSaldoCuenta(Integer cuentaId) {
        cuentaRepository.findById(cuentaId).ifPresent(cuenta -> {
            // Calculo en BD: evita cargar entidades en memoria y conflictos de session
            BigDecimal delta = transaccionRepository.sumarDeltaPorCuenta(cuentaId);
            if (delta == null) delta = BigDecimal.ZERO;
            BigDecimal base = cuenta.getSaldoInicial() != null ? cuenta.getSaldoInicial() : BigDecimal.ZERO;
            cuenta.setSaldo(base.add(delta));
            cuentaRepository.save(cuenta);
        });
    }
}
