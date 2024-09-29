package com.example.expensetrackerspring.rest.payload.response;

import com.example.expensetrackerspring.core.TransactionType;

public record SaveTransactionResponse(
        boolean successful,
        String message
) {
}
