package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.rest.payload.response.SavingsResponse;

import java.math.BigDecimal;

public interface UserService {

    void updateSavings(Long userId, BigDecimal newSavings);
    SavingsResponse getSavings(Long userId);
}
