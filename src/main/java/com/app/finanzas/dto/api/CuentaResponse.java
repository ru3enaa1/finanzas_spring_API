package com.app.finanzas.dto.api;

import java.math.BigDecimal;

public record CuentaResponse(
        Integer id,
        String tipo,
        BigDecimal saldo
) {
}
