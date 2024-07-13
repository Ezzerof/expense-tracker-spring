package com.example.expensetrackerspring.core.exceptions;

public class DuplicateExpenseException extends RuntimeException {
    public DuplicateExpenseException(String message) {
        super(message);
    }
}
