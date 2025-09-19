package com.app.finanzas.service;

import com.app.finanzas.entity.GastoFijo;
import com.app.finanzas.entity.RegistroGastoFijo;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.GastoFijoRepository;
import com.app.finanzas.repository.RegistroGastoFijoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GastoFijoService {

    private final GastoFijoRepository gastoFijoRepository;
    private final RegistroGastoFijoRepository registroGastoFijoRepository;

    public GastoFijoService(GastoFijoRepository gastoFijoRepository,
                            RegistroGastoFijoRepository registroGastoFijoRepository) {
        this.gastoFijoRepository = gastoFijoRepository;
        this.registroGastoFijoRepository = registroGastoFijoRepository;
    }

    public GastoFijo registrar(GastoFijo gastoFijo) {
        return gastoFijoRepository.save(gastoFijo);
    }

    public GastoFijo actualizar(GastoFijo gastoFijo) {
        return gastoFijoRepository.save(gastoFijo);
    }

    public void eliminar(Integer id) {
        gastoFijoRepository.deleteById(id);
    }

    public Optional<GastoFijo> buscarPorId(Integer id) {
        return gastoFijoRepository.findById(id);
    }

    public List<GastoFijo> listarPorUsuario(Usuario usuario) {
        return gastoFijoRepository.findByUsuario(usuario);
    }

    public List<GastoFijo> listarActivosPorUsuario(Usuario usuario) {
        return gastoFijoRepository.findByUsuarioAndActivoTrue(usuario);
    }

    public RegistroGastoFijo registrarPago(GastoFijo gastoFijo, int anio, int mes, BigDecimal montoPagado, boolean pagado, LocalDate fechaPago) {
        RegistroGastoFijo registro = registroGastoFijoRepository
                .findByGastoFijo_IdAndAnioAndMes(gastoFijo.getId(), anio, mes)
                .orElseGet(() -> {
                    RegistroGastoFijo nuevo = new RegistroGastoFijo();
                    nuevo.setGastoFijo(gastoFijo);
                    nuevo.setAnio(anio);
                    nuevo.setMes(mes);
                    return nuevo;
                });
        registro.setPagado(pagado);
        registro.setMontoPagado(montoPagado);
        registro.setFechaPago(fechaPago);
        registro.setAlertaGenerada(Boolean.FALSE);
        return registroGastoFijoRepository.save(registro);
    }

    public List<RegistroGastoFijo> listarRegistros(Integer gastoFijoId) {
        return registroGastoFijoRepository.findByGastoFijo_Id(gastoFijoId);
    }
}
