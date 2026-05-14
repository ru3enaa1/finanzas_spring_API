package com.app.finanzas.service;

import com.app.finanzas.entity.Alerta;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.AlertaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private AlertaService alertaService;

    @Test
    void registrarCreaAlertaCuandoNoExisteDuplicado() {
        Usuario usuario = new Usuario();
        usuario.setId(7);
        String mensaje = "Saldo objetivo alcanzado";

        when(alertaRepository.existsByUsuarioAndDescripcion(usuario, mensaje)).thenReturn(false);
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> {
            Alerta alerta = invocation.getArgument(0);
            alerta.setId(22);
            return alerta;
        });

        Alerta resultado = alertaService.registrar(usuario, mensaje);

        assertNotNull(resultado);
        assertEquals(22, resultado.getId());
        assertEquals(mensaje, resultado.getDescripcion());
        assertEquals(usuario, resultado.getUsuario());
        assertNotNull(resultado.getFechaHora());

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).save(captor.capture());
        LocalDateTime registrado = captor.getValue().getFechaHora();
        assertNotNull(registrado);
    }

    @Test
    void registrarNoGuardaCuandoExisteDuplicado() {
        Usuario usuario = new Usuario();
        usuario.setId(7);
        String mensaje = "Saldo objetivo alcanzado";

        when(alertaRepository.existsByUsuarioAndDescripcion(usuario, mensaje)).thenReturn(true);

        Alerta resultado = alertaService.registrar(usuario, mensaje);

        assertNull(resultado);
        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    void listarRecientesDelegadoEnRepositorio() {
        Usuario usuario = new Usuario();
        usuario.setId(8);
        List<Alerta> esperado = List.of(new Alerta(), new Alerta());
        when(alertaRepository.findTop5ByUsuarioOrderByFechaHoraDesc(usuario)).thenReturn(esperado);

        List<Alerta> resultado = alertaService.listarRecientes(usuario);

        assertEquals(esperado, resultado);
    }
}
