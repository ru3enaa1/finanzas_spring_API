package com.app.finanzas.config;

import java.util.List;

/**
 * Lista centralizada de rutas de modulos autenticados.
 * Usada por endpoints que aceptan un parametro 'redirect' (cambio de cuenta,
 * cambio de moneda, etc.) para validar contra open-redirect y mantener al
 * usuario en su modulo actual.
 *
 * Cuando se añada un nuevo modulo a la app, basta con añadirlo aqui — todos
 * los controllers que dependen de esta lista quedan actualizados.
 */
public final class RutasModulos {

    public static final List<String> AUTORIZADAS = List.of(
            "/dashboard",
            "/cuentas",
            "/transacciones",
            "/presupuestos",
            "/fondos",
            "/alertas",
            "/informes"
    );

    private RutasModulos() {}

    /** True si la ruta empieza con alguna de las autorizadas. */
    public static boolean esValida(String ruta) {
        if (ruta == null || ruta.isBlank()) return false;
        return AUTORIZADAS.stream().anyMatch(ruta::startsWith);
    }

    /** Devuelve la ruta si es valida, de lo contrario el fallback. */
    public static String resolverODefault(String ruta, String fallback) {
        return esValida(ruta) ? ruta : fallback;
    }
}
