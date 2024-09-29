package com.example.expensetrackerspring.rest.payload.response;

public record RemoveTransactionResponse(
        boolean success,
        String message
) {
}
