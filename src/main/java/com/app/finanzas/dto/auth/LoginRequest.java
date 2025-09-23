package com.app.finanzas.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO que recibe las credenciales para el inicio de sesion desde clientes externos.
 */
public class LoginRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe ser valido")
    private String correo;

    @NotBlank(message = "La contrasena es obligatoria")
    private String contrasena;

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
