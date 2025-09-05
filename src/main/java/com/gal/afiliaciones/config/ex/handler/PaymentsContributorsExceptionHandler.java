package com.gal.afiliaciones.config.ex.handler;

import com.gal.afiliaciones.config.ex.generalnovelty.PaymentsContributorsNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class PaymentsContributorsExceptionHandler {

    @ExceptionHandler(PaymentsContributorsNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentsContributorsNotFoundException(
            PaymentsContributorsNotFoundException ex) {
        
        log.warn("No se encontraron pagos de cotizantes: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", "/afiliaciones/general-novelty/payments-contributors");
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
} 