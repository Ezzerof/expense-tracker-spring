package com.example.expensetrackerspring.rest.payload.request;

import java.time.LocalDate;

public record DailyExpenseRequest(LocalDate date, Long userId) {
}
