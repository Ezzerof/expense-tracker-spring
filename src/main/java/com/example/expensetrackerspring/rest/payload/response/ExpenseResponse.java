package com.example.expensetrackerspring.rest.payload.response;

import com.example.expensetrackerspring.core.RecurrenceFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(Long id, String name, String description, BigDecimal amount, String category,
                              LocalDate startDate, LocalDate endDate, RecurrenceFrequency frequency) {
}