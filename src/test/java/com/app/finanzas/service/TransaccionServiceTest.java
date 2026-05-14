package com.app.finanzas.service;

import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.TipoTransaccion;
import com.app.finanzas.entity.Transaccion;
import com.app.finanzas.repository.CuentaRepository;
import com.app.finanzas.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private TransaccionService transaccionService;

    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        cuenta = new Cuenta();
        cuenta.setId(1);
        cuenta.setSaldo(BigDecimal.ZERO);
    }

    @Test
    void registrarIngresoActualizaSaldoCorrectamente() {
        Transaccion nuevoIngreso = buildTransaccion(TipoTransaccion.INGRESO, "150.00");

        when(transaccionRepository.save(any(Transaccion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cuentaRepository.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.findByCuenta_IdOrderByFechaDesc(cuenta.getId()))
                .thenReturn(List.of(
                        buildTransaccion(TipoTransaccion.INGRESO, "100.00"),
                        buildTransaccion(TipoTransaccion.GASTO, "40.00"),
                        nuevoIngreso
                ));

        transaccionService.registrar(nuevoIngreso);

        assertEquals(new BigDecimal("210.00"), cuenta.getSaldo());
        verify(transaccionRepository).save(nuevoIngreso);
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    void registrarGastoDisminuyeSaldoCorrectamente() {
        Transaccion nuevoGasto = buildTransaccion(TipoTransaccion.GASTO, "200.00");

        when(transaccionRepository.save(any(Transaccion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(cuentaRepository.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.findByCuenta_IdOrderByFechaDesc(cuenta.getId()))
                .thenReturn(List.of(
                        buildTransaccion(TipoTransaccion.INGRESO, "500.00"),
                        nuevoGasto
                ));

        transaccionService.registrar(nuevoGasto);

        assertEquals(new BigDecimal("300.00"), cuenta.getSaldo());
        verify(transaccionRepository).save(nuevoGasto);
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    void eliminarTransaccionRecalculaSaldo() {
        Transaccion transaccionExistente = buildTransaccion(TipoTransaccion.GASTO, "80.00");
        transaccionExistente.setId(5);

        when(transaccionRepository.findById(transaccionExistente.getId()))
                .thenReturn(Optional.of(transaccionExistente));
        when(cuentaRepository.findById(cuenta.getId())).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.findByCuenta_IdOrderByFechaDesc(cuenta.getId()))
                .thenReturn(List.of(
                        buildTransaccion(TipoTransaccion.INGRESO, "500.00"),
                        buildTransaccion(TipoTransaccion.GASTO, "120.00")
                ));

        transaccionService.eliminar(transaccionExistente.getId());

        assertEquals(new BigDecimal("380.00"), cuenta.getSaldo());
        verify(transaccionRepository).delete(transaccionExistente);
        verify(cuentaRepository).save(cuenta);
    }

    private Transaccion buildTransaccion(TipoTransaccion tipo, String monto) {
        Transaccion transaccion = new Transaccion();
        transaccion.setCuenta(cuenta);
        transaccion.setTipo(tipo);
        transaccion.setCategoria("Prueba");
        transaccion.setFecha(LocalDate.now());
        transaccion.setMonto(new BigDecimal(monto));
        transaccion.setDescripcion("Caso de prueba");
        return transaccion;
    }
}

