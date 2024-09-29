package com.example.expensetrackerspring.rest.payload.response;

import com.example.expensetrackerspring.core.TransactionType;
import com.example.expensetrackerspring.core.persistance.entity.Expense;

import java.math.BigDecimal;
import java.util.List;

public record DailyTransactionResponse(
        BigDecimal totalExpenses,
        List<Expense> expense,
        TransactionType transactionType
) {
}
