package com.example.expensetrackerspring.rest.payload.response;


public record SaveTransactionResponse(
        boolean successful,
        String message
) {
}
