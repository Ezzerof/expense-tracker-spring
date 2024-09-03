package com.example.expensetrackerspring.rest.payload.request;

public record MonthlySummaryRequest(Long userId, int year, int month) {
}
