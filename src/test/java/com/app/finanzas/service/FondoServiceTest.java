package com.app.finanzas.service;

import com.app.finanzas.entity.Fondo;
import com.app.finanzas.entity.RegistroFondo;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.FondoRepository;
import com.app.finanzas.repository.RegistroFondoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FondoServiceTest {

    @Mock
    private FondoRepository fondoRepository;

    @Mock
    private RegistroFondoRepository registroFondoRepository;

    @Mock
    private AlertaService alertaService;

    @InjectMocks
    private FondoService fondoService;

    private Fondo fondo;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1);
        usuario.setNombre("Usuario Prueba");

        fondo = new Fondo();
        fondo.setId(10);
        fondo.setUsuario(usuario);
        fondo.setNombre("Vacaciones");
        fondo.setMontoAnual(new BigDecimal("1000"));
    }

    @Test
    void registrarAporteGeneraAlertaCuandoSuperaOchentaPorCiento() {
        when(registroFondoRepository.findByFondo_IdAndAnioAndMes(fondo.getId(), 2025, 9))
                .thenReturn(Optional.empty());
        when(registroFondoRepository.save(any(RegistroFondo.class)))
                .thenAnswer(invocation -> {
                    RegistroFondo registro = invocation.getArgument(0);
                    registro.setId(55);
                    return registro;
                });
        when(registroFondoRepository.totalAportadoEnAnio(fondo.getId(), 2025))
                .thenReturn(new BigDecimal("850"));

        RegistroFondo resultado = fondoService.registrarAporte(fondo, 2025, 9, new BigDecimal("200"));

        assertNotNull(resultado);
        assertEquals(55, resultado.getId());
        verify(registroFondoRepository).save(any(RegistroFondo.class));
        verify(alertaService).registrar(eq(usuario), contains("85%"));
    }

    @Test
    void registrarAporteConMetaCumplidaCreaAlertaDeCienPorCiento() {
        when(registroFondoRepository.findByFondo_IdAndAnioAndMes(fondo.getId(), 2025, 10))
                .thenReturn(Optional.empty());
        when(registroFondoRepository.save(any(RegistroFondo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(registroFondoRepository.totalAportadoEnAnio(fondo.getId(), 2025))
                .thenReturn(new BigDecimal("1000"));

        fondoService.registrarAporte(fondo, 2025, 10, new BigDecimal("300"));

        ArgumentCaptor<String> mensajeCaptor = ArgumentCaptor.forClass(String.class);
        verify(alertaService).registrar(eq(usuario), mensajeCaptor.capture());
        assertEquals("Has cumplido el 100% de tu meta anual para el fondo 'Vacaciones'.", mensajeCaptor.getValue());
    }

    @Test
    void calcularPorcentajeAvanceDevuelveValorCorrecto() {
        when(registroFondoRepository.totalAportadoEnAnio(fondo.getId(), 2025))
                .thenReturn(new BigDecimal("250"));

        BigDecimal porcentaje = fondoService.calcularPorcentajeAvance(fondo, 2025);

        assertEquals(new BigDecimal("25.00"), porcentaje);
    }
}
