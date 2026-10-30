package com.adria.bank.transaction.web.rest;

import com.adria.bank.transaction.service.exception.AccountValidationException;
import com.adria.bank.transaction.service.exception.CurrencyMismatchException;
import com.adria.bank.transaction.service.exception.TransactionAmountExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionAmountExceededException.class)
    public ResponseEntity<Map<String, Object>> handleLimitExceeded(TransactionAmountExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", Instant.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Transaction Limit Exceeded",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler({CurrencyMismatchException.class, AccountValidationException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleBusinessValidation(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "timestamp", Instant.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Business Rule Violation",
                "message", ex.getMessage()
        ));
    }
}

