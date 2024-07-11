package com.example.expensetrackerspring.rest.payload.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(String name, String description, BigDecimal amount, String category, LocalDate date) {
}
