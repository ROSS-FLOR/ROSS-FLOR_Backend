package com.libreria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        System.out.println("CRITICAL EXCEPTION CAUGHT: " + ex.getMessage());
        ex.printStackTrace();
        return new ResponseEntity<>("Error Interno: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
