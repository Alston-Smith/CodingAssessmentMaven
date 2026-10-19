package com.alstonsmith.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Converts validation and business-rule failures into a consistent HTTP 400 response.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Maps domain errors such as duplicate usernames or invalid permissions into an API response.
     *
     * @param ex the validation or business-rule exception thrown by the service layer
     * @return a 400 Bad Request response containing the error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
