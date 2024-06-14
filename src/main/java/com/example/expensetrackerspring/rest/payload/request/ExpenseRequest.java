package com.example.expensetrackerspring.rest.payload.request;

import com.example.expensetrackerspring.core.Category;

import java.time.LocalDateTime;

public record ExpenseRequest(String name, String description, Category category, LocalDateTime dateTime) {
}
