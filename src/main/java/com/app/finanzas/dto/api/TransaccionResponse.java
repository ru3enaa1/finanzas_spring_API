package com.app.finanzas.dto.api;

import com.app.finanzas.entity.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionResponse(
        Integer id,
        Integer cuentaId,
        TipoTransaccion tipo,
        String categoria,
        LocalDate fecha,
        BigDecimal monto,
        String descripcion,
        boolean fijo
) {
}
