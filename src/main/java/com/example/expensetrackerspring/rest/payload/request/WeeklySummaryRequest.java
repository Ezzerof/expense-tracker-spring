package com.example.expensetrackerspring.rest.payload.request;

import java.time.LocalDate;

public record WeeklySummaryRequest(Long userId, LocalDate startOfTheWeek) {
}
