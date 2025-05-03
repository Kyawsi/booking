package com.booking.system.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", ZonedDateTime.now());
        error.put("status", ex.getStatus().value());
        error.put("error", ex.getStatus().getReasonPhrase());
        error.put("message", ex.getReason()); // <-- include your custom message
        error.put("path", request.getRequestURI());
        return new ResponseEntity<>(error, ex.getStatus());
    }
}
