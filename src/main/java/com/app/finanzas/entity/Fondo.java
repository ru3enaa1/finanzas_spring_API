package com.app.finanzas.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "fondo")
public class Fondo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String nombre;

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "monto_anual", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoAnual;

    @Size(max = 20)
    @Column(length = 20)
    private String color = "#5B8DEF";

    @Size(max = 200)
    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false)
    private boolean activo = true;

    @OneToMany(mappedBy = "fondo", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RegistroFondo> registros = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getMontoAnual() {
        return montoAnual;
    }

    public void setMontoAnual(BigDecimal montoAnual) {
        this.montoAnual = montoAnual;
    }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Set<RegistroFondo> getRegistros() {
        return registros;
    }

    public void setRegistros(Set<RegistroFondo> registros) {
        this.registros = registros;
    }
}
