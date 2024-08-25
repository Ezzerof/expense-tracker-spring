package com.example.expensetrackerspring.rest.payload.response;

import java.math.BigDecimal;

public record ExpenseDetail(String name, BigDecimal amount) {
}
