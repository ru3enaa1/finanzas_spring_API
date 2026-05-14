package com.app.finanzas.service;

import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.CuentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private CuentaService cuentaService;

    @Test
    void crearCuentaDelegadoEnRepositorio() {
        Cuenta cuenta = new Cuenta();
        when(cuentaRepository.save(cuenta)).thenReturn(cuenta);

        Cuenta resultado = cuentaService.crear(cuenta);

        assertSame(cuenta, resultado);
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    void ajustarSaldoActualizaEntidadAntesDeGuardar() {
        Cuenta cuenta = new Cuenta();
        cuenta.setSaldo(new BigDecimal("250.00"));
        BigDecimal nuevoSaldo = new BigDecimal("375.50");

        when(cuentaRepository.save(cuenta)).thenReturn(cuenta);

        cuentaService.ajustarSaldo(cuenta, nuevoSaldo);

        assertEquals(nuevoSaldo, cuenta.getSaldo());
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    void listarPorUsuarioUsaRepositorio() {
        Usuario usuario = new Usuario();
        List<Cuenta> cuentas = List.of(new Cuenta(), new Cuenta());
        when(cuentaRepository.findByUsuario(usuario)).thenReturn(cuentas);

        List<Cuenta> resultado = cuentaService.listarPorUsuario(usuario);

        assertEquals(cuentas, resultado);
    }
}
