package com.app.finanzas.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MonedaService {

    public static final String MONEDA_BASE = "COP";
    public static final String DEFAULT = "COP";

    private static final Map<String, BigDecimal> TASAS;
    private static final Map<String, String> SIMBOLOS;
    private static final Map<String, String> NOMBRES;

    static {
        TASAS = new LinkedHashMap<>();
        TASAS.put("COP", BigDecimal.ONE);
        TASAS.put("USD", new BigDecimal("0.00024"));
        TASAS.put("EUR", new BigDecimal("0.00022"));

        SIMBOLOS = Map.of("COP", "$", "USD", "$", "EUR", "€");
        NOMBRES  = Map.of("COP", "Peso colombiano",
                          "USD", "Dólar estadounidense",
                          "EUR", "Euro");
    }

    public boolean esValida(String moneda) {
        return moneda != null && TASAS.containsKey(moneda);
    }

    public String normalizar(String moneda) {
        return esValida(moneda) ? moneda : DEFAULT;
    }

    public BigDecimal tasa(String moneda) {
        return TASAS.getOrDefault(normalizar(moneda), BigDecimal.ONE);
    }

    public String simbolo(String moneda) {
        return SIMBOLOS.getOrDefault(normalizar(moneda), "$");
    }

    public String nombre(String moneda) {
        return NOMBRES.getOrDefault(normalizar(moneda), "Peso colombiano");
    }

    public List<String> disponibles() {
        return List.copyOf(TASAS.keySet());
    }

    public BigDecimal convertir(BigDecimal montoEnBase, String monedaDestino) {
        if (montoEnBase == null) return BigDecimal.ZERO;
        return montoEnBase.multiply(tasa(monedaDestino)).setScale(2, RoundingMode.HALF_UP);
    }
}
