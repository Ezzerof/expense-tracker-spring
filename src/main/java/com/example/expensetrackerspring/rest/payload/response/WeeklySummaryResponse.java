package com.example.expensetrackerspring.rest.payload.response;

import java.math.BigDecimal;

public record WeeklySummaryResponse(BigDecimal totalExpenses, BigDecimal totalIncome, BigDecimal balance) {
}
