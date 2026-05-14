package com.app.finanzas.service;

import com.app.finanzas.entity.Presupuesto;
import com.app.finanzas.entity.PresupuestoPeriodo;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.PresupuestoPeriodoRepository;
import com.app.finanzas.repository.PresupuestoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;
    private final PresupuestoPeriodoRepository presupuestoPeriodoRepository;

    public PresupuestoService(PresupuestoRepository presupuestoRepository,
                              PresupuestoPeriodoRepository presupuestoPeriodoRepository) {
        this.presupuestoRepository = presupuestoRepository;
        this.presupuestoPeriodoRepository = presupuestoPeriodoRepository;
    }

    public Presupuesto registrar(Presupuesto presupuesto) {
        return presupuestoRepository.save(presupuesto);
    }

    public Presupuesto actualizar(Presupuesto presupuesto) {
        return presupuestoRepository.save(presupuesto);
    }

    public void eliminar(Integer id) {
        presupuestoRepository.deleteById(id);
    }

    public Optional<Presupuesto> buscarPorId(Integer id) {
        return presupuestoRepository.findById(id);
    }

    public Optional<Presupuesto> buscarPorIdYUsuario(Integer id, Usuario usuario) {
        return presupuestoRepository.findByIdAndUsuario(id, usuario);
    }

    public List<Presupuesto> listarPorUsuario(Usuario usuario) {
        return presupuestoRepository.findByUsuario(usuario);
    }

    public List<Presupuesto> listarActivosPorUsuario(Usuario usuario) {
        return presupuestoRepository.findByUsuarioAndActivoTrue(usuario);
    }

    /**
     * Obtiene o crea el periodo (snapshot mensual) del presupuesto.
     * Se llama al crear transacciones para actualizar el gastado en tiempo real.
     */
    public PresupuestoPeriodo obtenerOCrearPeriodo(Presupuesto presupuesto, int anio, int mes) {
        return presupuestoPeriodoRepository
                .findByPresupuesto_IdAndAnioAndMes(presupuesto.getId(), anio, mes)
                .orElseGet(() -> {
                    PresupuestoPeriodo nuevo = new PresupuestoPeriodo();
                    nuevo.setPresupuesto(presupuesto);
                    nuevo.setAnio(anio);
                    nuevo.setMes(mes);
                    nuevo.setMontoLimiteSnapshot(presupuesto.getMontoEstimado());
                    nuevo.setMontoGastado(BigDecimal.ZERO);
                    return presupuestoPeriodoRepository.save(nuevo);
                });
    }

    public PresupuestoPeriodo sumarGastado(Presupuesto presupuesto, int anio, int mes, BigDecimal monto) {
        PresupuestoPeriodo periodo = obtenerOCrearPeriodo(presupuesto, anio, mes);
        BigDecimal nuevo = periodo.getMontoGastado().add(monto);
        periodo.setMontoGastado(nuevo.max(BigDecimal.ZERO));
        return presupuestoPeriodoRepository.save(periodo);
    }

    public PresupuestoPeriodo restarGastado(Presupuesto presupuesto, int anio, int mes, BigDecimal monto) {
        return sumarGastado(presupuesto, anio, mes, monto.negate());
    }

    public PresupuestoPeriodo registrarPago(Presupuesto presupuesto, int anio, int mes,
                                            BigDecimal montoPagado, boolean pagado, LocalDate fechaPago) {
        PresupuestoPeriodo periodo = obtenerOCrearPeriodo(presupuesto, anio, mes);
        periodo.setPagado(pagado);
        periodo.setMontoPagado(montoPagado);
        periodo.setFechaPago(fechaPago);
        periodo.setAlertaGenerada(Boolean.FALSE);
        return presupuestoPeriodoRepository.save(periodo);
    }

    public List<PresupuestoPeriodo> listarPeriodos(Integer presupuestoId) {
        return presupuestoPeriodoRepository.findByPresupuesto_Id(presupuestoId);
    }

    public Optional<PresupuestoPeriodo> buscarPeriodo(Integer presupuestoId, int anio, int mes) {
        return presupuestoPeriodoRepository.findByPresupuesto_IdAndAnioAndMes(presupuestoId, anio, mes);
    }
}
