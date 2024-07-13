package com.example.expensetrackerspring.core.exceptions;

public class InvalidExpenseDetailsException extends RuntimeException {
    public InvalidExpenseDetailsException(String message) {
        super(message);
    }
}
