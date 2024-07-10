package com.example.expensetrackerspring.core.service;

import com.example.expensetrackerspring.rest.payload.request.ExpenseRequest;
import com.example.expensetrackerspring.rest.payload.request.GetExpenseRequest;
import com.example.expensetrackerspring.rest.payload.request.RemoveExpenseRequest;
import com.example.expensetrackerspring.rest.payload.response.ExpenseResponse;
import com.example.expensetrackerspring.rest.payload.response.RemoveExpenseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ExpenseService {
    ExpenseResponse saveExpense(ExpenseRequest expenseRequest);

    Optional<ExpenseResponse> getExpense(GetExpenseRequest getExpenseRequest);

    Page<ExpenseResponse> getAllExpenses(Pageable pageable);

    RemoveExpenseResponse deleteExpense(RemoveExpenseRequest request);
}
