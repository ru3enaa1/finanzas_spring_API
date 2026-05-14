package com.app.finanzas.dto.api;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CuentaRequest(
        @NotBlank(message = "El tipo de cuenta es obligatorio")
        String tipo,
        BigDecimal saldo
) {
}
