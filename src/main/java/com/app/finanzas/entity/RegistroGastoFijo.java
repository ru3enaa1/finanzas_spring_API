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
import java.time.LocalDate;

@Entity
@Table(name = "registro_gasto_fijo",
        uniqueConstraints = @UniqueConstraint(name = "uk_registro_gasto_mes", columnNames = {"gasto_fijo_id", "anio", "mes"}))
public class RegistroGastoFijo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gasto_fijo_id", nullable = false)
    private GastoFijo gastoFijo;

    @NotNull
    @Column(nullable = false)
    private Integer anio;

    @NotNull
    @Min(1)
    @Max(12)
    @Column(nullable = false)
    private Integer mes;

    @NotNull
    @Column(nullable = false)
    private Boolean pagado = Boolean.FALSE;

    @DecimalMin("0.00")
    @Column(name = "monto_pagado", precision = 12, scale = 2)
    private BigDecimal montoPagado;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(name = "alerta_generada", nullable = false)
    private Boolean alertaGenerada = Boolean.FALSE;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public GastoFijo getGastoFijo() {
        return gastoFijo;
    }

    public void setGastoFijo(GastoFijo gastoFijo) {
        this.gastoFijo = gastoFijo;
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

    public Boolean getPagado() {
        return pagado;
    }

    public void setPagado(Boolean pagado) {
        this.pagado = pagado;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public Boolean getAlertaGenerada() {
        return alertaGenerada;
    }

    public void setAlertaGenerada(Boolean alertaGenerada) {
        this.alertaGenerada = alertaGenerada;
    }
}
