package com.app.finanzas.service;

import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1);
        usuario.setNombre("Laura");
        usuario.setCorreo("laura@example.com");
        usuario.setContrasenaPlano("claveSegura123");
    }

    @Test
    void registrarUsuarioCodificaContrasenaYLimpiaPlano() {
        when(passwordEncoder.encode("claveSegura123")).thenReturn("hash123");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario registrado = usuarioService.registrar(usuario);

        assertEquals("hash123", registrado.getContrasena());
        assertNull(registrado.getContrasenaPlano());
        verify(passwordEncoder).encode("claveSegura123");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("hash123", captor.getValue().getContrasena());
    }

    @Test
    void registrarUsuarioConPasswordCortaLanzaExcepcion() {
        usuario.setContrasenaPlano("corta");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrar(usuario));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void autenticarDevuelveUsuarioConCredencialesValidas() {
        Usuario persistido = new Usuario();
        persistido.setId(10);
        persistido.setCorreo(usuario.getCorreo());
        persistido.setContrasena("hash123");

        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(persistido));
        when(passwordEncoder.matches("claveSegura123", "hash123")).thenReturn(true);

        Optional<Usuario> resultado = usuarioService.autenticar(usuario.getCorreo(), "claveSegura123");

        assertTrue(resultado.isPresent());
        assertEquals(persistido, resultado.get());
    }

    @Test
    void autenticarDevuelveVacioConPasswordIncorrecta() {
        Usuario persistido = new Usuario();
        persistido.setCorreo(usuario.getCorreo());
        persistido.setContrasena("hash123");

        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(persistido));
        when(passwordEncoder.matches("claveSegura123", "hash123")).thenReturn(false);

        Optional<Usuario> resultado = usuarioService.autenticar(usuario.getCorreo(), "claveSegura123");

        assertFalse(resultado.isPresent());
    }

    @Test
    void actualizarPerfilConCambioDePasswordCodificaYLimpiaPlano() {
        usuario.setContrasena("viejoHash");
        usuario.setContrasenaPlano("nuevaClaveSegura");

        when(passwordEncoder.encode("nuevaClaveSegura")).thenReturn("nuevoHash");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario actualizado = usuarioService.actualizarPerfil(usuario, true);

        assertEquals("nuevoHash", actualizado.getContrasena());
        assertNull(actualizado.getContrasenaPlano());
        verify(passwordEncoder).encode("nuevaClaveSegura");
    }

    @Test
    void actualizarPerfilSinCambioDePasswordNoInvocaEncoder() {
        usuario.setContrasena("hashActual");
        usuario.setContrasenaPlano(null);

        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario actualizado = usuarioService.actualizarPerfil(usuario, false);

        assertEquals("hashActual", actualizado.getContrasena());
        verify(passwordEncoder, never()).encode(any());
    }
}
