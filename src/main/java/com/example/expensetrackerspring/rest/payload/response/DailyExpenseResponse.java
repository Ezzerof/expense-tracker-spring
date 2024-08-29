package com.example.expensetrackerspring.rest.payload.response;

import com.example.expensetrackerspring.core.persistance.entity.Expense;

import java.math.BigDecimal;
import java.util.List;

public record DailyExpenseResponse(BigDecimal totalExpenses, List<Expense> expense) {
}
