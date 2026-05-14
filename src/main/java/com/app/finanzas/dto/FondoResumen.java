package com.app.finanzas.dto;

import java.math.BigDecimal;

public class FondoResumen {

    private final Integer id;
    private final String nombre;
    private final BigDecimal montoAnual;
    private final BigDecimal totalAportado;
    private final BigDecimal porcentajeAvance;
    private final Integer anioEvaluado;

    public FondoResumen(Integer id,
                         String nombre,
                         BigDecimal montoAnual,
                         BigDecimal totalAportado,
                         BigDecimal porcentajeAvance,
                         Integer anioEvaluado) {
        this.id = id;
        this.nombre = nombre;
        this.montoAnual = montoAnual;
        this.totalAportado = totalAportado;
        this.porcentajeAvance = porcentajeAvance;
        this.anioEvaluado = anioEvaluado;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getMontoAnual() {
        return montoAnual;
    }

    public BigDecimal getTotalAportado() {
        return totalAportado;
    }

    public BigDecimal getPorcentajeAvance() {
        return porcentajeAvance;
    }

    public Integer getAnioEvaluado() {
        return anioEvaluado;
    }
}
