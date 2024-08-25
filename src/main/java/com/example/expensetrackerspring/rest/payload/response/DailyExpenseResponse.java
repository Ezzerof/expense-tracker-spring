package com.example.expensetrackerspring.rest.payload.response;

import java.math.BigDecimal;
import java.util.List;

public record DailyExpenseResponse(BigDecimal totalExpenses, List<ExpenseDetail> expense) {
}
