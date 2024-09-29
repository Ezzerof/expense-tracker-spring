package com.example.expensetrackerspring.rest.payload.request;

import com.example.expensetrackerspring.core.RecurrenceFrequency;
import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.core.persistance.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringTransactionRequest(
        String name,
        String description,
        String category,
        BigDecimal amount,
        LocalDate startDate,
        LocalDate endDate,
        RecurrenceFrequency recurrenceFrequency,
        User user,
        TransactionType transactionType
) {
}
