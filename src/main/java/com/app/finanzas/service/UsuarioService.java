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
        String rawPassword = usuario.getContrasena();
        if (rawPassword.length() < 12 || rawPassword.length() > 50) {
            throw new IllegalArgumentException("La contraseña debe tener entre 12 y 50 caracteres");
        }
        usuario.setContrasena(passwordEncoder.encode(rawPassword));
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    public boolean existeCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    public Usuario actualizarPerfil(Usuario usuario, boolean actualizarPassword) {
        if (actualizarPassword) {
            String rawPassword = usuario.getContrasena();
            if (rawPassword.length() < 12 || rawPassword.length() > 50) {
                throw new IllegalArgumentException("La contraseña debe tener entre 12 y 50 caracteres");
            }
            usuario.setContrasena(passwordEncoder.encode(rawPassword));
        }
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public void eliminar(Integer id) {
        usuarioRepository.deleteById(id);
    }
}
