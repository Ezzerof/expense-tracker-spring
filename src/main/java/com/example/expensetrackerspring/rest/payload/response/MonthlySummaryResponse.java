package com.example.expensetrackerspring.rest.payload.response;

import java.math.BigDecimal;

public record MonthlySummaryResponse(BigDecimal totalExpenses) {
}
