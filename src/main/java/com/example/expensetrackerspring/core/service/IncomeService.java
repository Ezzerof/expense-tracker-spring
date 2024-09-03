package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.rest.payload.request.*;
import com.example.expensetrackerspring.rest.payload.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IncomeService {

    void saveIncome(IncomeRequest request, Long userId);

    Optional<IncomeResponse> getIncome(GetIncomeRequest request, Long userId);

    Page<IncomeResponse> getAllIncomes(Pageable pageable, Long userId);

    Optional<IncomeResponse> editExpense(IncomeRequest expenseRequest, Long userId);

    RemoveIncomeResponse deleteExpense(RemoveIncomeRequest request, Long userId);

}
