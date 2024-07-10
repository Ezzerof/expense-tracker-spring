package com.example.expensetrackerspring.rest.payload.response;

import com.example.expensetrackerspring.core.Category;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(Long id, String name, String description, BigDecimal amount, Category category, LocalDate date) {
}