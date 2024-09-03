package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.rest.payload.request.GetIncomeRequest;
import com.example.expensetrackerspring.rest.payload.request.IncomeRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveIncomeRequest;
import com.example.expensetrackerspring.rest.payload.response.IncomeResponse;
import com.example.expensetrackerspring.rest.payload.response.RemoveIncomeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IncomeServiceImpl implements IncomeService{
    @Override
    public void saveIncome(IncomeRequest request, Long userId) {
        
    }

    @Override
    public Optional<IncomeResponse> getIncome(GetIncomeRequest request, Long userId) {
        return Optional.empty();
    }

    @Override
    public Page<IncomeResponse> getAllIncomes(Pageable pageable, Long userId) {
        return null;
    }

    @Override
    public Optional<IncomeResponse> editExpense(IncomeRequest expenseRequest, Long userId) {
        return Optional.empty();
    }

    @Override
    public RemoveIncomeResponse deleteExpense(RemoveIncomeRequest request, Long userId) {
        return null;
    }
}
