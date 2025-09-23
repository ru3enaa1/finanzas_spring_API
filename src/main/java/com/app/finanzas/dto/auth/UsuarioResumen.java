package com.app.finanzas.dto.auth;

/**
 * Datos basicos del usuario que se exponen en las respuestas del servicio de autenticacion.
 */
public class UsuarioResumen {

    private Integer id;
    private String nombre;
    private String correo;

    public UsuarioResumen() {
    }

    public UsuarioResumen(Integer id, String nombre, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
