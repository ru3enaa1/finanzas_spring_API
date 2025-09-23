package com.app.finanzas.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("activePath")
    public String exposeActivePath(HttpServletRequest request) {
        return request != null ? request.getRequestURI() : "";
    }
}
