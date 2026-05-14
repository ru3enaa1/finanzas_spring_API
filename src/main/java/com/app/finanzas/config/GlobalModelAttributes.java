package com.app.finanzas.config;

import com.app.finanzas.entity.Cuenta;
import com.app.finanzas.entity.Usuario;
import com.app.finanzas.service.CuentaService;
import com.app.finanzas.service.MonedaService;
import com.app.finanzas.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalModelAttributes {

    private static final Map<String, String> COLORES_POR_TIPO = Map.of(
            "Corriente",          "corriente",
            "Ahorro",             "ahorro",
            "Tarjeta de credito", "tarjeta",
            "Efectivo",           "efectivo",
            "Inversion",          "inversion"
    );

    private final UsuarioService usuarioService;
    private final CuentaService cuentaService;
    private final MonedaService monedaService;

    public GlobalModelAttributes(UsuarioService usuarioService,
                                 CuentaService cuentaService,
                                 MonedaService monedaService) {
        this.usuarioService = usuarioService;
        this.cuentaService = cuentaService;
        this.monedaService = monedaService;
    }

    @ModelAttribute("activePath")
    public String exposeActivePath(HttpServletRequest request) {
        return request != null ? request.getRequestURI() : "";
    }

    @ModelAttribute("usuario")
    public Usuario exposeUsuario(Authentication auth) {
        if (!isAuthenticated(auth)) return null;
        return usuarioService.buscarPorCorreo(auth.getName()).orElse(null);
    }

    @ModelAttribute("cuentas")
    public List<Cuenta> exposeCuentas(Authentication auth) {
        if (!isAuthenticated(auth)) return Collections.emptyList();
        return usuarioService.buscarPorCorreo(auth.getName())
                .map(cuentaService::listarPorUsuario)
                .orElse(Collections.emptyList());
    }

    @ModelAttribute("cuentaActivaId")
    public Integer exposeCuentaActivaId(HttpServletRequest request, Authentication auth) {
        if (!isAuthenticated(auth)) return null;
        return resolverCuentaActiva(request, auth).map(Cuenta::getId).orElse(null);
    }

    @ModelAttribute("cuentaActivaNombre")
    public String exposeCuentaActivaNombre(HttpServletRequest request, Authentication auth) {
        if (!isAuthenticated(auth)) return null;
        return resolverCuentaActiva(request, auth).map(Cuenta::getNombre).orElse(null);
    }

    /**
     * Resuelve la cuenta activa: sesion -> primera cuenta del usuario.
     * Si encuentra una valida y la sesion no la tenia, la persiste para que
     * todos los controllers la lean coherentemente.
     */
    private java.util.Optional<Cuenta> resolverCuentaActiva(HttpServletRequest request, Authentication auth) {
        HttpSession session = request.getSession(true);
        Integer activaId = (Integer) session.getAttribute("cuentaActivaId");

        List<Cuenta> cuentas = usuarioService.buscarPorCorreo(auth.getName())
                .map(cuentaService::listarPorUsuario)
                .orElse(Collections.emptyList());

        if (cuentas.isEmpty()) {
            if (activaId != null) session.removeAttribute("cuentaActivaId");
            return java.util.Optional.empty();
        }

        java.util.Optional<Cuenta> match = activaId == null
                ? java.util.Optional.empty()
                : cuentas.stream().filter(c -> c.getId().equals(activaId)).findFirst();

        if (match.isEmpty()) {
            Cuenta primera = cuentas.get(0);
            session.setAttribute("cuentaActivaId", primera.getId());
            return java.util.Optional.of(primera);
        }
        return match;
    }

    @ModelAttribute("coloresPorTipo")
    public Map<String, String> exposeColoresPorTipo() {
        return COLORES_POR_TIPO;
    }

    @ModelAttribute("monedaActual")
    public String exposeMonedaActual(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String moneda = session != null ? (String) session.getAttribute("monedaPreferida") : null;
        return monedaService.normalizar(moneda);
    }

    @ModelAttribute("simboloMoneda")
    public String exposeSimboloMoneda(HttpServletRequest request) {
        return monedaService.simbolo(exposeMonedaActual(request));
    }

    @ModelAttribute("tasaActual")
    public java.math.BigDecimal exposeTasaActual(HttpServletRequest request) {
        return monedaService.tasa(exposeMonedaActual(request));
    }

    @ModelAttribute("monedasDisponibles")
    public List<String> exposeMonedasDisponibles() {
        return monedaService.disponibles();
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }
}
