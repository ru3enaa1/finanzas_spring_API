package com.app.finanzas.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum TipoTransaccion {
    INGRESO("Ingreso"),
    GASTO("Gasto");

    private final String label;

    TipoTransaccion(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @JsonValue
    public String getJsonValue() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    @JsonCreator
    public static TipoTransaccion fromValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if ("EGRESO".equalsIgnoreCase(trimmed)) {
            return GASTO;
        }
        return Arrays.stream(values())
                .filter(tipo -> tipo.name().equalsIgnoreCase(trimmed) || tipo.getLabel().equalsIgnoreCase(trimmed))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de transaccion desconocido: " + value));
    }
}
