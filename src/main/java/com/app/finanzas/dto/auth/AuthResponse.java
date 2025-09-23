package com.app.finanzas.dto.auth;

/**
 * Respuesta estandarizada para los endpoints de registro e inicio de sesion.
 */
public class AuthResponse {

    private boolean exito;
    private String mensaje;
    private UsuarioResumen usuario;

    public static AuthResponse success(String mensaje, UsuarioResumen usuario) {
        AuthResponse response = new AuthResponse();
        response.setExito(true);
        response.setMensaje(mensaje);
        response.setUsuario(usuario);
        return response;
    }

    public static AuthResponse error(String mensaje) {
        AuthResponse response = new AuthResponse();
        response.setExito(false);
        response.setMensaje(mensaje);
        return response;
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public UsuarioResumen getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioResumen usuario) {
        this.usuario = usuario;
    }
}
