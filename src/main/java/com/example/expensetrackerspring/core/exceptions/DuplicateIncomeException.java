package com.example.expensetrackerspring.core.exceptions;

public class DuplicateIncomeException extends RuntimeException {
    public DuplicateIncomeException(String message) {
        super(message);
    }
}
