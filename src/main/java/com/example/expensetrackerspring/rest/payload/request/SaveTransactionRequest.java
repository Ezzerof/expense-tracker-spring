package com.example.expensetrackerspring.rest.payload.request;

import com.example.expensetrackerspring.core.RecurrenceFrequency;
import com.example.expensetrackerspring.core.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaveTransactionRequest(
        Long id,
        String name,
        String description,
        BigDecimal amount,
        String category,
        LocalDate startDate,
        LocalDate endDate,
        RecurrenceFrequency recurrenceFrequency,
        TransactionType transactionType
) {
}
