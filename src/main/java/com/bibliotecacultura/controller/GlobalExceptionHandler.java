package com.bibliotecacultura.controller;

import com.bibliotecacultura.exception.NegocioException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Any NegocioException (and its subtypes) that bubbles out of a controller
     * renders a simple error page rather than a 500 stack trace.
     */
    @ExceptionHandler(NegocioException.class)
    public String handleNegocio(NegocioException ex, Model model) {
        model.addAttribute("erro", ex.getMessage());
        return "error/negocio";
    }
}
