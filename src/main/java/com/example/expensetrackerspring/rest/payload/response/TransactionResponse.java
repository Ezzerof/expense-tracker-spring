package com.example.expensetrackerspring.rest.payload.response;

import com.example.expensetrackerspring.core.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        String name,
        String description,
        BigDecimal amount,
        String category,
        LocalDate startDate,
        LocalDate endDate,
        TransactionType transactionType
) {
}