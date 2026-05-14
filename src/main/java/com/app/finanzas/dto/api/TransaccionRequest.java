package com.app.finanzas.dto.api;

import com.app.finanzas.entity.TipoTransaccion;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransaccionRequest(
        @NotNull(message = "La cuenta es obligatoria")
        Integer cuentaId,
        @NotNull(message = "El tipo es obligatorio")
        TipoTransaccion tipo,
        @NotBlank(message = "La categoria es obligatoria")
        String categoria,
        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal monto,
        String descripcion,
        boolean fijo
) {
}
