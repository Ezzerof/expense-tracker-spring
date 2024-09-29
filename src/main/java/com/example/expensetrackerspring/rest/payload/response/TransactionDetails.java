package com.example.expensetrackerspring.rest.payload.response;

import com.example.expensetrackerspring.core.TransactionType;

import java.math.BigDecimal;

public record TransactionDetails(
        String name,
        BigDecimal amount,
        TransactionType transactionType
) {
}
