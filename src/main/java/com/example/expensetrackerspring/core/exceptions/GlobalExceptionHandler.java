package com.example.expensetrackerspring.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public final ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialException.class)
    public final ResponseEntity<String> handleInvalidCredentialException(InvalidCredentialException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateTransactionException.class)
    public final ResponseEntity<String> handleDuplicateTransactionException(DuplicateTransactionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidTransactionDetailsException.class)
    public final ResponseEntity<String> handleInvalidTransactionDetailsException(InvalidTransactionDetailsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public final ResponseEntity<String> handleTransactionNotFoundException(TransactionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateIncomeException.class)
    public final ResponseEntity<String> handleDuplicateIncomeException(DuplicateIncomeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(IncomeNotFoundException.class)
    public final ResponseEntity<String> handleIncomeNotFoundException(IncomeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidIncomeDetailsException.class)
    public final ResponseEntity<String> handleInvalidIncomeDetailsException(InvalidIncomeDetailsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidUsernameException.class)
    public final ResponseEntity<String> handleInvalidUsernameException(InvalidUsernameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}

