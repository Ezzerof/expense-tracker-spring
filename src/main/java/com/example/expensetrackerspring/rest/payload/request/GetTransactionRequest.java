package com.example.expensetrackerspring.rest.payload.request;

import com.example.expensetrackerspring.core.TransactionType;

public record GetTransactionRequest(
        Long id,
        TransactionType transactionType
) {
}
