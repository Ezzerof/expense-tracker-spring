package com.example.expensetrackerspring.core.exceptions;

public class InvalidTransactionDetailsException extends RuntimeException {
    public InvalidTransactionDetailsException(String message) {
        super(message);
    }
}
