package com.app.finanzas.dto;

import java.math.BigDecimal;

/**
 * Pieza de una barra segmentada: representa lo aportado/gastado desde una
 * cuenta especifica a un presupuesto o fondo, con el porcentaje del limite
 * que representa (para usar como width en CSS).
 *
 * Reusable por presupuestos y fondos.
 */
public class SegmentoCuenta {

    private final Integer cuentaId;
    private final String cuentaNombre;
    private final BigDecimal monto;          // original en COP
    private final BigDecimal montoConvertido; // en moneda visible
    private final double pctBar;              // % del limite total (para el width visual)

    public SegmentoCuenta(Integer cuentaId, String cuentaNombre,
                          BigDecimal monto, BigDecimal montoConvertido, double pctBar) {
        this.cuentaId = cuentaId;
        this.cuentaNombre = cuentaNombre;
        this.monto = monto;
        this.montoConvertido = montoConvertido;
        this.pctBar = pctBar;
    }

    public Integer getCuentaId() { return cuentaId; }
    public String getCuentaNombre() { return cuentaNombre; }
    public BigDecimal getMonto() { return monto; }
    public BigDecimal getMontoConvertido() { return montoConvertido; }
    public double getPctBar() { return pctBar; }
}
