package com.app.finanzas.service;

import com.app.finanzas.entity.Presupuesto;
import com.app.finanzas.entity.PresupuestoPeriodo;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.PresupuestoPeriodoRepository;
import com.app.finanzas.repository.PresupuestoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresupuestoServiceTest {

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private PresupuestoPeriodoRepository presupuestoPeriodoRepository;

    @InjectMocks
    private PresupuestoService presupuestoService;

    private Presupuesto presupuesto;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario();
        usuario.setId(3);
        usuario.setNombre("Carlos");

        presupuesto = new Presupuesto();
        presupuesto.setId(99);
        presupuesto.setNombre("Arriendo");
        presupuesto.setMontoEstimado(new BigDecimal("500.00"));
        presupuesto.setUsuario(usuario);
    }

    @Test
    void registrarPagoCreaRegistroNuevoConValores() {
        when(presupuestoPeriodoRepository.findByPresupuesto_IdAndAnioAndMes(99, 2025, 9))
                .thenReturn(Optional.empty());
        when(presupuestoPeriodoRepository.save(any(PresupuestoPeriodo.class)))
                .thenAnswer(invocation -> {
                    PresupuestoPeriodo periodo = invocation.getArgument(0);
                    periodo.setId(15);
                    return periodo;
                });

        PresupuestoPeriodo guardado = presupuestoService.registrarPago(
                presupuesto,
                2025,
                9,
                new BigDecimal("120.50"),
                true,
                LocalDate.of(2025, 9, 5)
        );

        assertNotNull(guardado.getId());
        assertEquals(2025, guardado.getAnio());
        assertEquals(9, guardado.getMes());
        assertTrue(guardado.getPagado());
        assertEquals(new BigDecimal("120.50"), guardado.getMontoPagado());
        assertEquals(LocalDate.of(2025, 9, 5), guardado.getFechaPago());
        assertEquals(Boolean.FALSE, guardado.getAlertaGenerada());

        ArgumentCaptor<PresupuestoPeriodo> captor = ArgumentCaptor.forClass(PresupuestoPeriodo.class);
        verify(presupuestoPeriodoRepository).save(captor.capture());
        assertSame(presupuesto, captor.getValue().getPresupuesto());
    }

    @Test
    void registrarPagoActualizaRegistroExistente() {
        PresupuestoPeriodo existente = new PresupuestoPeriodo();
        existente.setId(30);
        existente.setPresupuesto(presupuesto);
        existente.setAnio(2025);
        existente.setMes(8);
        existente.setMontoGastado(BigDecimal.ZERO);

        when(presupuestoPeriodoRepository.findByPresupuesto_IdAndAnioAndMes(99, 2025, 8))
                .thenReturn(Optional.of(existente));
        when(presupuestoPeriodoRepository.save(existente)).thenReturn(existente);

        PresupuestoPeriodo actualizado = presupuestoService.registrarPago(
                presupuesto,
                2025,
                8,
                new BigDecimal("95.00"),
                false,
                null
        );

        assertSame(existente, actualizado);
        assertEquals(new BigDecimal("95.00"), actualizado.getMontoPagado());
        assertEquals(Boolean.FALSE, actualizado.getPagado());
        assertNull(actualizado.getFechaPago());
        assertEquals(Boolean.FALSE, actualizado.getAlertaGenerada());
        verify(presupuestoPeriodoRepository).save(existente);
    }
}
