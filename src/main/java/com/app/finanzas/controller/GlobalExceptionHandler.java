package com.app.finanzas.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ModelAndView handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        LOGGER.warn("Request [{}] failed: {}", request.getRequestURI(), ex.getMessage(), ex);
        ModelAndView modelAndView = new ModelAndView("error/general");
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        modelAndView.addObject("mensaje", ex.getMessage());
        modelAndView.addObject("ruta", request.getRequestURI());
        return modelAndView;
    }

    /**
     * Captura cualquier otra excepcion (NPE, JPA, etc.) que no fue manejada arriba,
     * logueando el stacktrace completo para diagnostico.
     */
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleUnexpected(RuntimeException ex, HttpServletRequest request) {
        LOGGER.error("Request [{}] failed with unexpected error", request.getRequestURI(), ex);
        ModelAndView modelAndView = new ModelAndView("error/general");
        modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        modelAndView.addObject("mensaje", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        modelAndView.addObject("ruta", request.getRequestURI());
        return modelAndView;
    }
}
