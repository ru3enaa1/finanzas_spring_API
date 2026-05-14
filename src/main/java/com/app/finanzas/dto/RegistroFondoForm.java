package com.app.finanzas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RegistroFondoForm {

    @NotNull
    @Min(2000)
    @Max(2100)
    private Integer anio;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer mes;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal monto;

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

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}

