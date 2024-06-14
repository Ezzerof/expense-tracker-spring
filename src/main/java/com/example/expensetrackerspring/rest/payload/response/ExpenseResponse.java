package com.example.expensetrackerspring.rest.payload.response;

import com.example.expensetrackerspring.core.Category;

import java.time.LocalDateTime;

public record ExpenseResponse(String name, String description, Category category, LocalDateTime dateTime) {
}