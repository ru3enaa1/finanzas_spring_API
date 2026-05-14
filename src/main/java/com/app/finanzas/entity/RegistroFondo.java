package com.app.finanzas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "registro_fondo", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"fondo_id", "anio", "mes"})
})
public class RegistroFondo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fondo_id", nullable = false)
    private Fondo fondo;

    @NotNull
    @Min(2000)
    @Max(2100)
    @Column(nullable = false)
    private Integer anio;

    @NotNull
    @Min(1)
    @Max(12)
    @Column(nullable = false)
    private Integer mes;

    @NotNull
    @DecimalMin("0.01")
    @Column(name = "monto_aportado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoAportado;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Fondo getFondo() {
        return fondo;
    }

    public void setFondo(Fondo fondo) {
        this.fondo = fondo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public BigDecimal getMontoAportado() {
        return montoAportado;
    }

    public void setMontoAportado(BigDecimal montoAportado) {
        this.montoAportado = montoAportado;
    }
}
