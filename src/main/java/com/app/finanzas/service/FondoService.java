package com.app.finanzas.service;

import com.app.finanzas.entity.Fondo;
import com.app.finanzas.entity.RegistroFondo;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.FondoRepository;
import com.app.finanzas.repository.RegistroFondoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FondoService {

    private final FondoRepository fondoRepository;
    private final RegistroFondoRepository registroFondoRepository;
    private final AlertaService alertaService;

    public FondoService(FondoRepository fondoRepository,
                        RegistroFondoRepository registroFondoRepository,
                        AlertaService alertaService) {
        this.fondoRepository = fondoRepository;
        this.registroFondoRepository = registroFondoRepository;
        this.alertaService = alertaService;
    }

    public Fondo crear(Fondo fondo) {
        return fondoRepository.save(fondo);
    }

    public Fondo actualizar(Fondo fondo) {
        return fondoRepository.save(fondo);
    }

    public void eliminar(Fondo fondo) {
        fondoRepository.delete(fondo);
    }

    public Optional<Fondo> buscarPorId(Integer id) {
        return fondoRepository.findById(id);
    }

    public Optional<Fondo> buscarPorIdYUsuario(Integer id, Usuario usuario) {
        return fondoRepository.findByIdAndUsuario(id, usuario);
    }

    public List<Fondo> listarPorUsuario(Usuario usuario) {
        if (usuario == null) {
            return Collections.emptyList();
        }
        return fondoRepository.findByUsuarioOrderByNombreAsc(usuario);
    }

    public boolean existeNombreParaUsuario(Usuario usuario, String nombre) {
        return fondoRepository.existsByUsuarioAndNombreIgnoreCase(usuario, nombre);
    }

    public RegistroFondo registrarAporte(Fondo fondo, int anio, int mes, BigDecimal monto) {
        RegistroFondo registro = registroFondoRepository
                .findByFondo_IdAndAnioAndMes(fondo.getId(), anio, mes)
                .orElseGet(() -> {
                    RegistroFondo nuevo = new RegistroFondo();
                    nuevo.setFondo(fondo);
                    nuevo.setAnio(anio);
                    nuevo.setMes(mes);
                    return nuevo;
                });
        registro.setMontoAportado(monto);
        RegistroFondo guardado = registroFondoRepository.save(registro);

        BigDecimal porcentaje = calcularPorcentajeAvance(fondo, anio);
        if (porcentaje.compareTo(BigDecimal.valueOf(100)) >= 0) {
            alertaService.registrar(fondo.getUsuario(),
                    "Has cumplido el 100% de tu meta anual para el fondo '" + fondo.getNombre() + "'.");
        } else if (porcentaje.compareTo(BigDecimal.valueOf(80)) >= 0) {
            alertaService.registrar(fondo.getUsuario(),
                    "Has cumplido el " + porcentaje.setScale(0, RoundingMode.HALF_UP)
                            + "% de tu meta anual para el fondo '" + fondo.getNombre() + "'.");
        }
        return guardado;
    }

    public List<RegistroFondo> listarRegistros(Fondo fondo) {
        return registroFondoRepository.findByFondo_IdOrderByAnioDescMesDesc(fondo.getId());
    }

    public BigDecimal calcularPorcentajeAvance(Fondo fondo, int anio) {
        BigDecimal montoObjetivo = fondo.getMontoAnual();
        if (montoObjetivo == null || BigDecimal.ZERO.compareTo(montoObjetivo) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalAportado = registroFondoRepository.totalAportadoEnAnio(fondo.getId(), anio);
        if (totalAportado == null) {
            totalAportado = BigDecimal.ZERO;
        }
        return totalAportado.multiply(BigDecimal.valueOf(100))
                .divide(montoObjetivo, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotalAportado(Fondo fondo, int anio) {
        BigDecimal total = registroFondoRepository.totalAportadoEnAnio(fondo.getId(), anio);
        return total != null ? total : BigDecimal.ZERO;
    }
}
