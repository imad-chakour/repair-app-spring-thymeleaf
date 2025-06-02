package com.gestionmagasin.repairapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        // Log the full stack trace
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        
        logger.error("Unexpected error: {}\nStack trace: {}", e.getMessage(), stackTrace);
        
        // Add both the error message and stack trace to the model
        model.addAttribute("error", e.getMessage());
        model.addAttribute("stackTrace", stackTrace);
        model.addAttribute("exception", e);
        
        return "error";
    }
} 