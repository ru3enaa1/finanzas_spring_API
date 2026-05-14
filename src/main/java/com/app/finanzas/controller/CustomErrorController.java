package com.app.finanzas.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.boot.web.servlet.error.ErrorController;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = statusCode instanceof Integer
                ? HttpStatus.resolve((Integer) statusCode)
                : HttpStatus.INTERNAL_SERVER_ERROR;

        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        ModelAndView modelAndView = new ModelAndView("error/general");
        modelAndView.setStatus(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR);
        modelAndView.addObject("mensaje", message != null ? message : "Ha ocurrido un error inesperado.");
        modelAndView.addObject("ruta", path != null ? path : "No disponible");
        return modelAndView;
    }
}
