package com.example.expensetrackerspring.rest.payload.request;

import com.example.expensetrackerspring.core.TransactionType;

import java.time.LocalDate;

public record DailyTransactionRequest(
        LocalDate date,
        Long userId,
        TransactionType transactionType
) {
}
