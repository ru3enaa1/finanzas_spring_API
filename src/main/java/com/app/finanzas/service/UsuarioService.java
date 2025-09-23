package com.app.finanzas.service;

import com.app.finanzas.entity.Usuario;
import com.app.finanzas.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrar(Usuario usuario) {
        String rawPassword = usuario.getContrasenaPlano();
        validarLongitudPassword(rawPassword);
        usuario.setContrasena(passwordEncoder.encode(rawPassword));
        Usuario guardado = usuarioRepository.save(usuario);
        guardado.setContrasenaPlano(null);
        return guardado;
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    public boolean existeCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    /**
     * Valida las credenciales recibidas desde la API y devuelve el usuario cuando son correctas.
     */
    public Optional<Usuario> autenticar(String correo, String contrasena) {
        return usuarioRepository.findByCorreo(correo)
                .filter(usuario -> passwordEncoder.matches(contrasena, usuario.getContrasena()));
    }

    public Usuario actualizarPerfil(Usuario usuario, boolean actualizarPassword) {
        if (actualizarPassword) {
            String rawPassword = usuario.getContrasenaPlano();
            validarLongitudPassword(rawPassword);
            usuario.setContrasena(passwordEncoder.encode(rawPassword));
        }
        Usuario actualizado = usuarioRepository.save(usuario);
        actualizado.setContrasenaPlano(null);
        return actualizado;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public void eliminar(Integer id) {
        usuarioRepository.deleteById(id);
    }

    private void validarLongitudPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 12 || rawPassword.length() > 50) {
            throw new IllegalArgumentException("La contrasena debe tener entre 12 y 50 caracteres");
        }
    }
}
