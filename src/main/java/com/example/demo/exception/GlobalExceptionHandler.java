package com.example.demo.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "Recurso no encontrado");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error-custom";
    }

    @ExceptionHandler(OutOfStockException.class)
    public String handleOutOfStock(OutOfStockException ex, Model model) {
        model.addAttribute("errorTitle", "Sin stock disponible");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error-custom";
    }

    @ExceptionHandler(BusinessRuleException.class)
    public String handleBusinessRule(BusinessRuleException ex, Model model) {
        model.addAttribute("errorTitle", "Operación no permitida");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/error-custom";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, Model model) {
        model.addAttribute("errorTitle", "Error inesperado");
        model.addAttribute("errorMessage", "Ha ocurrido un error interno en el servidor. Por favor, intente de nuevo más tarde.");
        // Un-comment during development to debug locally if needed
        ex.printStackTrace();
        return "error/error-custom";
    }
}
