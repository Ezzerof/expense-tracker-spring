package com.example.expensetrackerspring.rest.payload.response;

import java.math.BigDecimal;

public record SavingsResponse(BigDecimal savings, String yearMonth) {
}
